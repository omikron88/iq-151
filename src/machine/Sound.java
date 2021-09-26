package machine;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author tmilata
 */
public class Sound extends Thread {
    //sample rate vystupniho zvuku   
    static double sampleRate = 96000.0;    
    //velikost bufferu pro 20ms zvuku
    static int BUFFER_SIZE = 1*(int)sampleRate/50;    
    //pocet taktu CPU na jeden sampl(jeden zapis) v bufferu
    public static int nOneSampleStates =(int)((double)(5*40*1024)/(((double)(sampleRate/10))));
    //pocitadlo taktu
    public static int nDecrementSampleStates = 0;
    

    //bufer pro prehravani a pro plneni, budou se swapovat
    public SndBuffer playBuffer = null;
    public SndBuffer fillBuffer = null;
    //prehravac zvuku
    SourceDataLine audioLine = null;
    Object objDeinit=new Object();
    int FULL_BUFFER_SIZE=6*BUFFER_SIZE;
    
    PlayBuffer playThread = null;
    //indikator, je-li povolen zvuk
    private boolean bEnabled;
    
    public int nSampleReturnedCorrection=0;
    //udrzuje datovy tok na zukove zarizeni nepreruseny
    public SoundGuard guard=null;
    //limit pod ktery nesmi klesnout buffer zvukoveho zarizeni, jinak se zacnou posilat 0
    public int limit5ms = 2*(int)sampleRate/100;
    //blok ticha - same 0
    private byte[] silent = new byte[limit5ms];
    //indikuje prvni zaslani zvuku z IQ, aby bylo mozno vlozit 10 sec. ticha na zacatek
    public boolean bFirstFill=true;
      
    
    //Buffer pro zvukova data
    public class SndBuffer{
        byte[] data = null;
        int nPosition;
        boolean bIsFull;
        boolean bBit;
        int nInTime;
        int dmpcnt;
        public boolean bState=false;

        public SndBuffer() {
            data = new byte[2 * BUFFER_SIZE];//generuji 16-bitovy wav, proto 2*velikost bufferu
            dmpcnt=0;
            nPosition = 0;
            bIsFull = false;
            bBit = false;
            nInTime = 0;//zajistuje, aby v jednom casovem useku byl vytvoren pouze 1 sampl
            bState=false;
        }
        
        //pouze pro debug
        public void dumpBuffer() {
            
            String strDmpfile = utils.Config.getMyPath() + "dump" + String.valueOf(dmpcnt) + ".txt";
         //   System.out.println("Dump"+String.valueOf(dmpcnt));
            dmpcnt++;

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(strDmpfile);
                fos.write(data);
                fos.close();
            } catch (Exception ex) {
                
            }
        }
               
        //metoda pro periodicke vkladani predchozi hodnoty do bufferu
        public boolean putToBuffer() {
            boolean bRet = bIsFull; 
            if (nInTime==0) {
             bRet = putToBuffer(bBit);
            }
            nInTime = 0;
            return bRet;
        }

        //metoda pro vlozeni zmeny do bufferu pri OUT
        public boolean putToBuffer(boolean bStatusBit) {  
            bBit = bStatusBit;
            if (nInTime==0) {
                if (!bIsFull) {
                    if (bStatusBit) {
                        data[nPosition] = (byte) 0xff;
                        data[nPosition + 1] = (byte) 0xf1;
                    } else {
                        data[nPosition] = (byte) 0x00;
                        data[nPosition + 1] = (byte) 0x00;
                    }                    
                    nPosition += 2;
                    if (nPosition >= 2 * BUFFER_SIZE) {
                        bIsFull = true;                    
                    }
                }
            }
            nInTime++;
            return bIsFull;
        }
        
        
        public boolean isFull() {
            return bIsFull;
        }
        

        //vyprazdni buffer, bit reproduktoru ponecha jak je
        public void emptyBuffer() {
            nPosition = 0;
            Arrays.fill(data, (byte) 0);
            bIsFull = false;
            nInTime = 0;
        }
    }


    public Sound() {
        bEnabled=true;
        Arrays.fill(silent,(byte)0);       
    }
    
    public void setEnabled(boolean inEnabled){
        bEnabled=inEnabled;
    }

    public void init() {
        //inicializace 2 bufferu pro preklapeni - jeden vzdy hraje, druhy se plni
        playBuffer = new SndBuffer();
        fillBuffer = new SndBuffer();
        openAudio();
        if (bEnabled) {
            startPlaying();
            //thread, ktery kazde 2 sec. kontroluje, jestli je v bufferu zvuk.zarizeni dost dat
            Timer tim = new Timer("GuardTimer");
            guard = new SoundGuard();
            tim.scheduleAtFixedRate(guard, 1, 2);
        }
    }
    
    public void deinit() { 
        guard.cancel();
        guard=null;
        playThread.setDataReady();
        playThread.setFinish();
        try {
            synchronized(objDeinit){
             objDeinit.wait();
            }
        } catch (InterruptedException ex) {         
        }
        closeAudio();
        playBuffer = null;
        fillBuffer = null;
    }
    
    public boolean isEnabled(){
        return bEnabled;
    }
    

    //otevre audio prehravac
    public void openAudio() {
        if (bEnabled) {
            final boolean bigEndian = false;
            final boolean signed = true;
            final int bits = 16;
            final int channels = 1;
            AudioFormat format = new AudioFormat((float) sampleRate, bits, channels, signed, bigEndian);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try {
                audioLine = (SourceDataLine) AudioSystem.getLine(info);
                audioLine.open(format, FULL_BUFFER_SIZE);
                audioLine.start();
            } catch (Exception ex) {
                //pocitac nema zvukovou kartu, nebo neumi uvedeny audioformat
                bEnabled = false;
            }
        }
    }
    
    //ukonci audio prehravac
    public void closeAudio() {
        if (bEnabled) {
            if (audioLine != null) {
                audioLine.flush();
                audioLine.drain();
                audioLine.close();
                audioLine = null;
            }
        }
    }

    //vyprazdni playBuffer s prenosem stavu bitu
    //a prohodi oba buffery
     public void switchBuffers() {
         if(bFirstFill){
             //vlozim 10ms ticha na zacatek, abych mel pak cas pri vypadku provest nejake kroky
             bFirstFill=false;
             fillBufferByZero();
         }
         //pokud neni buffer vyplnen cely, musim doplnit az do konce, jinak by bylo slyset chrceni
         if(fillBuffer.nPosition<2*BUFFER_SIZE){             
          for(int i=fillBuffer.nPosition;i<2*BUFFER_SIZE;i+=2){
             fillBuffer.putToBuffer();
          }   
         } 
         boolean bBitLoc=fillBuffer.bBit;
         int nInTimeLoc=fillBuffer.nInTime;
        SndBuffer tmpBuffer=playBuffer;
        tmpBuffer.emptyBuffer();
        tmpBuffer.bBit=bBitLoc;
        tmpBuffer.nInTime=nInTimeLoc;
        playBuffer=fillBuffer;
        fillBuffer=tmpBuffer;        
    }
    
    
    //vraci pocet taktu CPU pro 1 sampl, kvuli presnosti se hodnota co 3 samply meni, aby se dosahlo
    //poctu taktu 21.3333333 pomoci integer cisel
    public int getOneSampleState() {
        int nRet = nOneSampleStates;       
        nSampleReturnedCorrection++;
        //korekce taktu 21.33333+21.33333+21.33333=21+21+22
        if (nSampleReturnedCorrection >= 3) {
            nRet++;
            nSampleReturnedCorrection = 0;
        }
        return nRet;
    }
    
   //dava prehravacimu threadu vedet, ze jsou data v bufferu pripravena pro prenos na zarizeni
   public void setDataReady() {
        if (playThread != null) {
            playThread.setDataReady();
        }
    }

    public void startPlaying() {
        if (audioLine == null) {
          openAudio();  
        }
        //spusteni threadu pro plneni prehravaciho zarizeni
        playThread = new PlayBuffer();
        playThread.start();
    }
  
    //posila Buffer na zarizeni v samostatnem threadu
    private class PlayBuffer extends Thread {
        private boolean bRunning=true;
        private CountDownLatch nTransfer = new CountDownLatch(1);
        
        public void setDataReady() {
           nTransfer.countDown();
        }
      
        public void setFinish() {
           bRunning=false;
        }
         
        public void run() {
            if (audioLine != null) {
                while (bRunning) {                  
                    try {
                        nTransfer.await();
                    } catch (InterruptedException ex) {
                    }
                     audioLine.write(playBuffer.data, 0, playBuffer.data.length);  
                    nTransfer=new CountDownLatch(1);
                }
                synchronized(objDeinit){
                    objDeinit.notifyAll();
                }
            }
        }
    }
   
    public void fillBufferByZero() {
        if (!bFirstFill) {
            if (audioLine != null) {
                int nBufferFilled = FULL_BUFFER_SIZE - audioLine.available();
                if (nBufferFilled <= limit5ms) {
                    //neco je spatne, v bufferu zvuk.zarizeni je uz jen 5ms dat
                    //poslu 10ms ticha
                    audioLine.write(silent, 0, silent.length);
                    audioLine.write(silent, 0, silent.length);
                }
            }
        }
    }

    //thread hlidajici nepreruseny tok dat do zvukoveho zarizeni
    //jinak by v Linuxu bylo slyset chrceni
    public class SoundGuard extends TimerTask {
       private long now, diff;
        @Override
        public synchronized void run() {           
            now = System.currentTimeMillis();
            diff = now - scheduledExecutionTime();
            if (diff < 2) {
                try {
                    Thread.sleep(2 - diff);
                } catch (InterruptedException ex) {
                }
                fillBufferByZero();
            }

        }
    }

}
