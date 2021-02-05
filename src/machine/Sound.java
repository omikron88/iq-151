package machine;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    //static double sampleRate = 44100.0;    
    static double sampleRate = 96000.0;    
    //velikost bufferu pro 50ms zvuku
    //static int BUFFER_SIZE = 2205; 
    static int BUFFER_SIZE = 4800;    
    //velikost bufferu v milisekundach   
    static int BufferMillisecLength=(int)((double)BUFFER_SIZE/((double)sampleRate/1000));
    //pocet taktu CPU na jeden sampl(jeden zapis) v bufferu
    public static int nOneSampleStates = (int)((double)(5*40*1024)/(((double)100*BUFFER_SIZE)/(double)BufferMillisecLength));
    //pocitadlo taktu
    public static int nDecrementSampleStates = 0;

    //bufer pro prehravani a pro plneni, budou se swapovat
    public SndBuffer playBuffer = null;
    public SndBuffer fillBuffer = null;
    //prehravac zvuku
    SourceDataLine audioLine = null;
    PlayBuffer playThread = null;
    //indikator, je-li povolen zvuk
    private boolean bEnabled;
    
    //Buffer pro zvukova data
    public class SndBuffer extends Thread{
        byte[] data = null;
        int nPosition;
        private long lEmptyTime;
        private long lFullTime;
        boolean bIsFull;
        boolean bBit;
        int nInTime;
        int nChanges;
        int dmpcnt;

        public SndBuffer() {
            data = new byte[2 * BUFFER_SIZE];//generuji 16-bitovy wav, proto 2*velikost bufferu
            dmpcnt=0;
            nPosition = 0;
            lFullTime = 0;
            lEmptyTime = System.currentTimeMillis();
            bIsFull = false;
            bBit = false;
            nInTime = 0;
            nChanges=0;
        }
        //pouze pro debug
        public void dumpBuffer() {
            String strDmpfile = utils.Config.getMyPath() + "dump" + String.valueOf(dmpcnt) + ".txt";
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
             nChanges++;
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
                        data[nPosition] = (byte) 0x03;
                        data[nPosition + 1] = (byte) 0xd0;
                    }                    
                    nPosition += 2;
                    if (nPosition >= 2 * BUFFER_SIZE) {
                        bIsFull = true;
                        lFullTime = System.currentTimeMillis();
                        //System.out.println("playthread=" + (lFullTime - lEmptyTime));
                        if(playThread!=null)
                        {
                            //nutne kvuli nekterym implementacim Javy, ktere neumi vypnout audio bez lupnuti.
                            playThread.stop();
                        }                       
                        // dumpBuffer();
                        switchBuffers();
                        playThread = null;
                        playThread = new PlayBuffer(lFullTime - lEmptyTime);
                        playThread.start();
                    }
                }
            }
            nInTime++;
            return bIsFull;
        }
        
        public boolean isFull() {
            return bIsFull;
        }

        public void empty() {
            empty(null);
        }

        public void empty(SndBuffer bfrLast) {
            nPosition = 0;
            Arrays.fill(data, (byte) 0);
            lFullTime = 0;
            lEmptyTime = System.currentTimeMillis();
            bIsFull = false;
            bBit = false;
            nInTime = 0;
            nChanges=0;
            if (bfrLast != null) {
                bBit = bfrLast.bBit;
            }
        }
    }

    //prehraje Buffer v samostatnem threadu
    private class PlayBuffer extends Thread {
        long nSleep;//pocet milisekund, ktere trvalo naplnit buffer
        public PlayBuffer(long inSleep){
            nSleep=inSleep;
        }
        public void run() {
            //buffer obsahuje data pro urcity pocet milisekund,
            //pokud byl naplnen driv, je treba pockat az dohraje predchozi buffer
            if(nSleep<BufferMillisecLength){
                try {
                    Thread.sleep(BufferMillisecLength-nSleep);
                } catch (InterruptedException ex) {                   
                }
            }

            if (playBuffer.isFull()) {
                 audioLine.write(playBuffer.data, 0, playBuffer.nPosition);            
            }
        }
    }

    public Sound() {
        bEnabled=true;
    }
    
    public void setEnabled(boolean inEnabled){
        bEnabled=inEnabled;
    }

    public void init() {
        //inicializace 2 bufferu pro preklapeni - jeden vzdy hraje, druhy se plni
        playBuffer = new SndBuffer();
        fillBuffer = new SndBuffer();
        openAudio();
    }
    
    public void deinit() {
        closeAudio();
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
                audioLine.open(format, 2 * BUFFER_SIZE);
                audioLine.start();
            } catch (Exception ex) {
                //pocitac nema zvukovou kartu, nebo neumi uvedeny audioformat
                bEnabled = false;
            }
        }
    }

    //ukonci audio prehravac
    public void closeAudio() {
        if(bEnabled){
         audioLine.flush();
         audioLine.close();
        }
    }

    //vyprazdni playBuffer s prenosem stavu bitu
    //a prohodi oba buffery
    public void switchBuffers() {
        SndBuffer tmpBuffer=playBuffer;
        playBuffer.empty(fillBuffer);
        playBuffer=fillBuffer;
        fillBuffer=tmpBuffer;
    }
    

}
