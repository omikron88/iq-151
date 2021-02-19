package machine;

import java.io.FileOutputStream;
import java.util.Arrays;
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
    //static double sampleRate = 44100.0;
    //velikost bufferu pro 20ms zvuku
    static int BUFFER_SIZE = 1*(int)sampleRate/50;    
    //velikost bufferu v milisekundach   
    static int BufferMillisecLength=(int)((double)BUFFER_SIZE/((double)sampleRate/1000));
    //pocet taktu CPU na jeden sampl(jeden zapis) v bufferu
    public static int nOneSampleStates =(int)((double)(5*40*1024)/(((double)100*BUFFER_SIZE)/(double)BufferMillisecLength));
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
    
    public int nSampleReturnedCorrection=0;
    
    //Buffer pro zvukova data
    public class SndBuffer{
        byte[] data = null;
        int nPosition;
        public long lEmptyTime;
        private long lFullTime;
        boolean bIsFull;
        boolean bBit;
        int nInTime;
        int nChanges;
        int dmpcnt;
        public boolean bState=false;

        public SndBuffer() {
            data = new byte[2 * BUFFER_SIZE];//generuji 16-bitovy wav, proto 2*velikost bufferu
            dmpcnt=0;
            nPosition = 0;
            lFullTime = 0;
            lEmptyTime = System.currentTimeMillis();
            bIsFull = false;
            bBit = false;
            nInTime = 0;//zajistuje, aby v jednom casovem useku byl vytvoren pouze 1 sampl
            nChanges=0;
            bState=false;
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
                        data[nPosition] = (byte) 0x00;
                        data[nPosition + 1] = (byte) 0x00;
                    }                    
                    nPosition += 2;
                    if (nPosition >= 2 * BUFFER_SIZE) {
                        bIsFull = true;
                        lFullTime = System.currentTimeMillis();

                        //System.out.println("playthread=" + (lFullTime - lEmptyTime)+",changes="+(nChanges+1)+",bBit="+bBit);
                        
                        // dumpBuffer(); 
                        if(playThread!=null){
                            try {
                                playThread.join();
                                playThread=null;
                            } catch (InterruptedException ex) {
                               
                            }
                        } 
                        switchBuffers();
                        playThread = new PlayBuffer();
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
        
        //vyprazdni buffer, bit reproduktoru nastavi na 0
        public void emptyZeroBit() {
           emptyNoBitChange();
           bBit = false; 
        } 

        //vyprazdni buffer, bit reproduktoru prenese z predaneho bufferu
        public void emptyTransferBit(SndBuffer bfrLast) {
            emptyZeroBit();
            if (bfrLast != null) {
                bBit = bfrLast.bBit;
            }
        }
        
        //vyprazdni buffer, bit reproduktoru ponecha jak je
        public void emptyNoBitChange() {
            nPosition = 0;
            Arrays.fill(data, (byte) 0);
            lFullTime = 0;
            lEmptyTime = System.currentTimeMillis();
            bIsFull = false;
            nInTime = 0;
            nChanges=0;
        }
    }

    //prehraje Buffer v samostatnem threadu
    private class PlayBuffer extends Thread {
        public void run() {
            if (playBuffer != null) {
                if (playBuffer.isFull()) {
                    if (audioLine != null) {
                        audioLine.write(playBuffer.data, 0, playBuffer.nPosition);
                    }
                }
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
                audioLine.open(format, BUFFER_SIZE);
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
            if (playThread != null) {
                try {
                    playThread.join(); //pockam az dohraje zbytek zvuku, pak teprve muzu ukoncit
                    playThread = null;
                } catch (InterruptedException ex) {

                }
            }
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
        SndBuffer tmpBuffer=playBuffer;
        playBuffer.emptyTransferBit(fillBuffer);
        playBuffer=fillBuffer;
        fillBuffer=tmpBuffer;
        fillBuffer.bState=false;
    }
    
    //vraci pocet taktu pro 1 sampl, kvuli presnosti se hodnota co 3 samply meni, aby se dosahlo
    //poctu taktu 21.3333333 pomoci integer cisel
    public int getOneSampleState() {
        int nRet = (int) ((double) (5 * 40 * 1024) / (((double) 100 * BUFFER_SIZE) / (double) BufferMillisecLength));
        nSampleReturnedCorrection++;
        //korekce taktu 21.33333+21.33333+21.33333=21+21+22
        if (nSampleReturnedCorrection >= 3) {
            nRet++;
            nSampleReturnedCorrection = 0;
        }
        return nRet;
    }

}

