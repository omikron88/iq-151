/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import gui.JIQScreen;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import gui.Debugger;
import gui.JIQ151;
import java.util.Arrays;
import javax.swing.JLabel;


/**
 *
 * @author Administrator
 */
public class Iq extends Thread 
 implements MemIoOps, NotifyOps, Pio8255Notify {
    
    public JIQScreen scr;
    private BufferedImage img;
    int col0=0;int col1=255+256*255+65536*255;
    int sirka=560;int vyska=272;
    int ofsx=20;int ofsy=8;
    private byte chars[];
    private byte vm[][];
    private Config cfg;
    private Keyboard key;
    public Memory mem;
    private Timer tim;
    private IqTimer task;
    public  Clock clk;
    public I8085 cpu;
    private Pic ic;
    private Pio8255 pio;
    private Tape tap;
    private Grafik graf;
    private SDRom sdrom;
    public Io8272 floppyCtrl;
    private Debugger deb;
    private JIQ151 frame;
    public static long nSpeedPercent=0;
    public int nSpeedPercentUpdateMaxCycles=50;
    public int nSpeedPercentUpdateDec=nSpeedPercentUpdateMaxCycles;
    public long nLastSeen=System.currentTimeMillis()-1;
    public Sound snd;
    public int nAutoRunBreakAddress=0;
    public boolean bResetInProgress=false;
    
    public static long lLastOut=0;
    public static long lTimeHelp=0;

    
    int lastchar=0;
		
    private boolean paused;
    private int port80;
    private boolean khz1;
    private boolean zobrgr;
    private boolean tapein;
    private boolean tapeo, tapeout;
    private boolean tapestart = false;
    private boolean tapeinv = true;
    private final int[] vm64 = new int[2048];
    private final int[] znsada = new int[1024];
    private final int[] znsadainv = new int[1024];
    private final int[] dstg = new int[1024];
    private final int[] dstg64 = new int[2048];
    public JLabel lblLed=null;
    public FloppyData floppyA=new FloppyData();
    public FloppyData floppyB=new FloppyData();
    
    public boolean bSav=false;
    public boolean bAutoRunAfterReset;
    
    int nSpeed=2;
    
    public Iq() {                
        img = new BufferedImage(sirka, vyska, BufferedImage.TYPE_BYTE_BINARY);
        cfg = new Config();
        utils.Config.LoadConfig();
        cfg.setMain((byte)utils.Config.mainmodule);
        cfg.setGrafik(utils.Config.grafik);
        cfg.setSDRom(utils.Config.sdrom);
        cfg.setSDRomAutorun(utils.Config.sdromautorun);
        cfg.setFelAutorun(utils.Config.felautorun);
        cfg.setAmosAutorun(utils.Config.amosautorun);
        cfg.setMem64(utils.Config.mem64);
        cfg.setVideo((byte)utils.Config.video64);
        cfg.setMonitor((byte)utils.Config.monitor);
        cfg.setAudio(utils.Config.audio);
        cfg.setDisc2(utils.Config.bDisc2);
        cfg.setSmartKbd(utils.Config.smartkeyboard);
        mem = new Memory(cfg);
        chars = mem.getChars();
        vm = mem.getVRam();
        tim = new Timer("IQclock");
        clk = new Clock();
        cpu = new I8085(clk, this, this);
        ic = new Pic();
        ic.setCPU(cpu);
        pio = new Pio8255(this);
        key = new Keyboard();
        key.setMachine(this);
        key.setPic(ic);
        graf = new Grafik();
        graf.Init();
        snd = new Sound();
        snd.setEnabled(cfg.getAudio());
        snd.init();        
        sdrom=null;
        if(cfg.getSDRom()){
         sdrom = new SDRom(this);
        }        
        if (cfg.disc2) {
            floppyCtrl = new Io8272(this);
            floppyCtrl.I8272reset();
        } else {
            floppyCtrl = null;
        }
        tap = new Tape(this);
        
        paused = true;

    }
    
     public void setDebugger(Debugger indeb){
        deb=indeb;
    }
     
     public void setFrame(JIQ151 inJIQ){
        frame=inJIQ;
    }
     
    public void setSpeed(int inSpeed){
     nSpeed=inSpeed;
    }
    
    public Debugger getDebugger(){
        return deb;
    }
    
    public SDRom getSDRom(){
        return sdrom;
    }

    public void setSDRomLED(JLabel inLed){
        lblLed=inLed;
        if(sdrom!=null){
         sdrom.setLED(lblLed);
        }
    }
    
    public void setConfig(Config c) {
        if (!cfg.equals(c)) {
            cfg = c;
            Reset(false);
        }
    }
    
    public Config getConfig() {
        return cfg;
    } 
    
    public byte[][] getVideoMemory(){
        return vm;
    }
    
    public void setScreen(JIQScreen screen) {
        scr = screen;
    }
   
    public BufferedImage getImage() {
        return img;
    }
    
    public Keyboard getKeyboard() {
        return key;
    }
    
    public void clearScreen() {
        for (int i=0; i<vyska; i++){
         for(int j=0; j<sirka; j++){
          img.setRGB(j, i, col0);   
         }    
        }
    }
    
    public final void Reset(boolean dirty) {
        if (!bResetInProgress) {
            //vice, nez 1 nesmi byt reset spusten
            bResetInProgress = true;
            stopEmulation();
            if (cfg.getSDRom()) {
                if (sdrom != null) {
                    sdrom.stopThread();
                }
                sdrom = new SDRom(this);
                sdrom.setLED(lblLed);
                sdrom.start();
            }
            if (cfg.disc2) {
                if (floppyCtrl == null) {
                    floppyCtrl = new Io8272(this);
                    floppyCtrl.I8272reset();
                }
            } else {
                floppyCtrl = null;
            }
            mem.Reset(dirty);
            if (zobrgr) {
                Arrays.fill(graf.GVRam, (byte) 0);
            }
            port80 = 0;
            mem.setBootstrap(true);
            clk.reset();
            ic.Reset();
        
            cpu.reset();
            pio.Reset();
            key.Reset();
            // pro zrychlení vykreslování se potřebuju zbavit BYTE u chars
            // a připravit tabulky s adresama
            for (int i = 0; i < 1024; i++) {
                znsada[i] = (chars[i] & 255);
                znsadainv[i] = 255 - znsada[i];
                dstg[i] = 2 * (i & 0x1f) + ((i >> 5) * 512);
                dstg64[i] = (i & 0x3f) + ((i >> 6) * 512);
                dstg64[i + 1024] = dstg64[i] + 8192;
            }

            //nastaveni Autorun SDROM
        if ((cfg.getSDRom()) && (cfg.getSDRomAutorun())) {
            if (bAutoRunAfterReset) {
                //smazu predchozi BP, pokud ho nebylo dosazeno
                cpu.setBreakpoint(nAutoRunBreakAddress, false);
            }
            if (cfg.getMain() == 1) {
                //Basic 6
                nAutoRunBreakAddress = 0xccd4;
            } else {
                if (cfg.getMain() == 2) {
                    //Basic G
                    nAutoRunBreakAddress = 0xcd79;
                } else {
                    //Monitor
                    nAutoRunBreakAddress = 0xf1ce;
                }
            }
            cpu.setBreakpoint(nAutoRunBreakAddress, true);
            bAutoRunAfterReset = true;
        }          
   
            cpu.nAudioStatesNextCycleCorrection = 0;
            cpu.nStartAudioTStates = clk.getTstates();
            if ((snd.isEnabled()) && (!cfg.getAudio())) {
                //musim disablovat audio
                snd.setEnabled(cfg.getAudio());
                snd.deinit();
            } else {
                if ((!snd.isEnabled()) && (cfg.getAudio())) {
                    //musim spustit audio
                    snd.setEnabled(cfg.getAudio());
                    snd.init();
                }
            }
            startEmulation();
            bResetInProgress = false;
        }
    }
    
    public synchronized void startEmulation() {
        frame.setPauseIcon(true);
        if (!paused)
            return;
        
        paused = false;

        task = new IqTimer(this);
        tim.scheduleAtFixedRate(task, 20, 20);
       }
    
    public synchronized void stopEmulation() {        
        frame.setPauseIcon(false);
        key.StopPasteFromClipboard();
        if (paused)
            return;
        
        paused = true;
        task.cancel();
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public void ms20() {
        //aktualizuji rychlost emulace
        long nNowSeen=System.currentTimeMillis();
        if((nNowSeen-nLastSeen)>0){
         nSpeedPercent=nSpeedPercent+(int)(200000/(nNowSeen-nLastSeen));
        }
        nLastSeen=nNowSeen;
        nSpeedPercentUpdateDec--;
        //je treba zobrazit - prumer z 30 po sobe jdoucich hodnot + 20x predchozi zobrazena hodnota (aby byly zmeny plynulejsi)
        if(nSpeedPercentUpdateDec<=0){
         nSpeedPercentUpdateDec=nSpeedPercentUpdateMaxCycles;
         //zaokrouhleni nahoru
         nSpeedPercent=((nSpeedPercent/(10*nSpeedPercentUpdateMaxCycles))+7)/10;
         frame.setTitle("jIQ151 - "+(nSpeed*nSpeedPercent)/2+"%");         
         nSpeedPercent=2000*nSpeedPercent;
         nSpeedPercentUpdateDec-=20;
        }
        
        if (paused) 
            return;
        
        if((snd.isEnabled()&&(nSpeed==2))){
             snd.switchBuffers();
             snd.setDataReady();
            }
        
        ic.assertInt(6);
       for (int rychl = 0; rychl < nSpeed; rychl++) { 
        for (int t = 0; t < 20; t++) {               // half period of 1kHz
            if (!paused) {
                cpu.execute(clk.getTstates() + 1024); // is 1024 of 2MHz Tstates
                khz1 = !khz1;
                if (khz1) {
                    tapeout = tapeo;
                }
            }

        }
       }
                

        redrawScreen();
    }

    /** Prekresli celou obrazovku z videopameti. Volano kazdych 20 ms z ms20()
     *  a take po nacteni snapshotu, kdy emulace muze byt pozastavena. */
    public void redrawScreen() {
// zjisti jestli se má zobrazovat grafika        
        zobrgr=graf.Enabled&&graf.ShowGR&&cfg.grafik;
        
        if (cfg.getVideo()==cfg.VIDEO32) {
// VIDEO 32
            int b; int a;int dst;boolean inver;
            for(int ad=0; ad<1024; ad++) {
              b = vm[0][ad]& 0xff;
              if (b>127){inver=true;b&=127;}else{inver=false;}
              vlozdovram32(ad,b*8,inver);
                        }
         }  //video32
        else {   
// Video64
            int b;boolean inver;boolean druhy=false;
            for(int ad=0; ad<256; ad++) {
                 vm64[ad]=vm[0][ad]& 0xff;   
                 vm64[ad+256]=vm[0][ad+256]& 0xff; 
                 vm64[ad+512]=vm[0][ad+512]& 0xff; 
                 vm64[ad+768]=vm[0][ad+768]& 0xff;
                 vm64[ad+1024]=vm[1][ad]& 0xff; 
                 vm64[ad+1280]=vm[1][ad+256]& 0xff; 
                 vm64[ad+1536]=vm[1][ad+512]& 0xff; 
                 vm64[ad+1792]=vm[1][ad+768]& 0xff; 
                }
            boolean zdvoj=false;
            for(int ad=0; ad<2048; ad++) {
              if ((ad&63)==0){zdvoj=false;}  
              b = vm64[ad];
              if (b>127){b&=127;inver=true;}else{inver=false;}
//              zde test na zdvojování!!!
              if (b==127){b=32;}
              if (b==124){b=32;}
              if(!zdvoj) {
                      vlozdovram64(ad,b*8,inver);
                         }
               else { //na liché pozici nedělat nic
                  if (!druhy){
                          int aa=vm64[ad-1];
                          if ((aa&127)==127){aa=vm64[ad];}
                          if (aa>127){aa&=127;inver=true;}else{inver=false;}
                          if ((aa==124)||(aa==13)){zdvoj=false;vlozdovram64(ad,256,inver);}
                          else {vlozdovram64zdv(ad,aa*8,inver);druhy=true; }
                             }
                  else{druhy=false;}
                    }
                 b = vm64[ad]&127;
                 if (b==127){zdvoj=cfg.V64ena32;
                             if((ad&1)==1){druhy=true;vlozdovram64zdv(ad,256,inver);}
                            }
                 } //for
        }  //video64


// a překreslit celou plochu        
        scr.repaint();
        
    }
 
   private void vlozdovram32(int adl,int src,boolean inverl) {
       int a;int b;int c;
       int dst=dstg[adl];
       b=(adl>>5)*8+ofsy;
       for(int ii=0; ii<8; ii++) {
        a=(adl&31)* 16+ofsx;   
        if(inverl){ c = znsadainv[src++];}
          else{ c = znsada[src++];}
        if ((c&128)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&64)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&32)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&16)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&8)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&4)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&2)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&1)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a, b, col1);}
        a=(adl&31)* 16+ofsx;
        //grafik
        if (zobrgr){
          c=graf.GVRam[dst++];
          if ((c&128)== 128){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&64)== 64){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&32)== 32){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&16)== 16){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&8)== 8){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&4)== 4){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&2)== 2){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&1)== 1){img.setRGB(a, b, col1);}
          a+=1;
          c=graf.GVRam[dst];
          if ((c&128)== 128){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&64)== 64){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&32)== 32){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&16)== 16){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&8)== 8){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&4)== 4){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&2)== 2){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&1)== 1){img.setRGB(a, b, col1);}
          dst+=63;
         }
         b+=1;
        }
    }
   
      private void vlozdovram64zdv(int adl,int src,boolean inverl) {
       int a;int b;int c;
       int dst=dstg64[adl];
       b=(adl>>6)*8+ofsy;
       for(int ii=0; ii<8; ii++) {
        a=(adl&63)* 8+ofsx;   
        if(inverl){ c = znsadainv[src++];}
          else{ c = znsada[src++];}
        if ((c&128)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&64)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&32)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&16)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&8)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&4)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&2)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a++, b, col1);}
        if ((c&1)== 0){img.setRGB(a++, b, col0);img.setRGB(a++, b, col0);}
        else{img.setRGB(a++, b, col1);img.setRGB(a, b, col1);}
        a=(adl&63)* 8+ofsx;
        //grafik
        if (zobrgr){
          c=graf.GVRam[dst++];
          if ((c&128)== 128){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&64)== 64){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&32)== 32){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&16)== 16){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&8)== 8){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&4)== 4){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&2)== 2){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&1)== 1){img.setRGB(a, b, col1);}
          a+=1;
          if ((adl&63)!=63) {
          c=graf.GVRam[dst];
          if ((c&128)== 128){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&64)== 64){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&32)== 32){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&16)== 16){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&8)== 8){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&4)== 4){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&2)== 2){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&1)== 1){img.setRGB(a, b, col1);}
          }  // test poslední znak
          dst+=63;
         }
         b+=1;
        }
    } 
   
    private void vlozdovram64(int adl,int src,boolean inverl) {
       int a;int b;int c;
       int dst=dstg64[adl];
       b=(adl>>6)*8+ofsy;
       for(int ii=0; ii<8; ii++) {
        a=(adl&63)* 8+ofsx;   
        if(inverl){ c = znsadainv[src++];}
          else{ c = znsada[src++];}
        if ((c&128)== 0){img.setRGB(a++, b, col0);}else{img.setRGB(a++, b, col1);}
        if ((c&64)== 0){img.setRGB(a++, b, col0);} else{img.setRGB(a++, b, col1);}
        if ((c&32)== 0){img.setRGB(a++, b, col0);} else{img.setRGB(a++, b, col1);}
        if ((c&16)== 0){img.setRGB(a++, b, col0);} else{img.setRGB(a++, b, col1);}
        if ((c&8)== 0){img.setRGB(a++, b, col0);}  else{img.setRGB(a++, b, col1);}
        if ((c&4)== 0){img.setRGB(a++, b, col0);}  else{img.setRGB(a++, b, col1);}
        if ((c&2)== 0){img.setRGB(a++, b, col0);}  else{img.setRGB(a++, b, col1);}
        if ((c&1)== 0){img.setRGB(a, b, col0);}  else{img.setRGB(a, b, col1);}
        a=(adl&63)* 8+ofsx;
        
        //grafik
        if (zobrgr){
          c=graf.GVRam[dst];
          if ((c&128)== 128){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&64)== 64){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&32)== 32){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&16)== 16){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&8)== 8){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&4)== 4){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&2)== 2){img.setRGB(a, b, col1);}
          a+=1;
          if ((c&1)== 1){img.setRGB(a, b, col1);}
          a+=1;
          dst+=64;
         }
         //vymazat případné zdvojené znaky za řádkem - ToMi zakomentovano, maze posledni sloupec u Video64
         //if ((adl&63)==63){for (int kk=0;kk<8;kk++){img.setRGB(a++, b, col0);}}
         b+=1;
        }
    }

    
    
    @Override
    public void run() {
        startEmulation();
 
        boolean forever = true;
        while(forever) {
            try {
                sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }

    @Override
    public int fetchOpcode(int address) {
//        System.out.println(String.format("PC: %04X", address));
        clk.addTstates(4);
        if (cpu.isIntAck())
                return ic.getIntAckCycle();
        int opcode = mem.readByte(address) & 0xff;
        return opcode;
    }

    @Override
    public int peek8(int address) {
        clk.addTstates(3);
        int value = mem.readByte(address) & 0xff;
        return value;
    }

    @Override
    public void poke8(int address, int value) {
//        System.out.println(String.format("Poke: %04X,%02X (%04X)", address,value,cpu.getRegPC()));
           boolean bExe=true;
       
        if(utils.Config.bBP6){
        //memorywrite BP
         if(utils.Config.nBP6Address==address){
            stopEmulation();
            int bpAdd=cpu.getRegPC()-1;
            cpu.setRegPC(bpAdd);         
            getDebugger().showDialog();
            cpu.bMemBP=true; //ukonci provadeni instrukci
            bExe=false;
         }
        }
        if(bExe){
         clk.addTstates(3);
         mem.writeByte(address, (byte) value);
        }
    }

    @Override
    public int peek16(int address) {
        clk.addTstates(6);
        if (cpu.isIntAck()) {
            cpu.setRegPC((cpu.getRegPC()-2) & 0xffff); // correct PC+2 during CALL 
            return ic.getIntAckCycle();
        }
        int lsb = mem.readByte(address) & 0xff;
        address = (address+1) & 0xffff;
        return ((mem.readByte(address) << 8) & 0xff00 | lsb);
    }

    @Override
    public void poke16(int address, int word) {
        clk.addTstates(6);
        mem.writeByte(address, (byte) word);
        address = (address+1) & 0xffff;
        mem.writeByte(address, (byte) (word >>> 8));
    }

    @Override
    public int inPort(int port) {
        int rVal=0xff;
        clk.addTstates(4);
        port &= 0xff;       
        switch (port) {
            case 0x84:
                return pio.CpuRead(pio.PP_PortA);
            case 0x85:
                return pio.CpuRead(pio.PP_PortB);
            case 0x86:
                return pio.CpuRead(pio.PP_PortC);
            case 0x87:
                return pio.CpuRead(pio.PP_CWR);
            case 0x88:
                return ic.readPortA0();
            case 0x89:
                return ic.readPortA1(); 
            case 0xAA:
            case 0xAB:                      
                   if(cfg.disc2){
                       rVal=floppyCtrl.I8272in(port);
                   }
                   return rVal;
            case 0xAC:  
                   rVal=0;
                   if(cfg.disc2){                      
                      rVal=(floppyCtrl.dis2mnt==true ? 1 : 0);
                   }
                   return rVal;
            case 0xD4:
                if (cfg.grafik) {
                    return graf.rpD4();
                } else {
                    return 0xFF;
                }
                         
            case 0xF8:
                if (cfg.getSDRom()) {
                    rVal=sdrom.getPio().CpuRead(sdrom.getPio().PP_PortA);
                    sdrom.yield();
                }
                return rVal;
            case 0xF9:
                if (cfg.getSDRom()) {
                    rVal=sdrom.getPio().CpuRead(sdrom.getPio().PP_PortB);
                }
                return rVal;
            case 0xFA:
                if (cfg.getSDRom()) {  
                    rVal = sdrom.getPio().CpuRead(sdrom.getPio().PP_PortC);                    
                    sdrom.yield();
                }
                return rVal;
            case 0xFB:
                if (cfg.getSDRom()) {
                    rVal=sdrom.getPio().CpuRead(sdrom.getPio().PP_CWR);
                }
                return rVal;
            case 0xFC:
            case 0xFD:
            case 0xFE:
            case 0xFF:
                if (cfg.getVideo()==cfg.VIDEO64){return 0xFE;}
                else{return 0xff;}
        }
                
        return 0xff;
    }

    @Override
    public void outPort(int port, int value) {
        clk.addTstates(4);
        port &= 0xff;       
        switch (port) {
            case 0x80:
                port80 = value;
                mem.setBootstrap((value & 0x01)==0);
                break;
            case 0x84:
                pio.CpuWrite(pio.PP_PortA, value);
                break;
            case 0x85:
                pio.CpuWrite(pio.PP_PortB, value);
                break;
            case 0x86:
                pio.CpuWrite(pio.PP_PortC, value);
                //obsluha zvuku
                if(snd.isEnabled()){
                    snd.fillBuffer.putToBuffer((value & 0x08)!=0);                 
                }
                break;
            case 0x87:                
                //obsluha zvuku v pripade, ze je port C nastavovan pres port CWR                
                int nCorrected = value & 0x8f;
                if ((nCorrected == 6) || (nCorrected == 7)) {
                    if (snd.isEnabled()) {
                        snd.fillBuffer.putToBuffer((value & 0x01) != 0);
                    }
                } else {
                    pio.CpuWrite(pio.PP_CWR, value);
                }

                break;
            case 0x88:
                ic.writePortA0(value);
                break;
            case 0x89:
                ic.writePortA1(value);
                break;
            case 0xAB:
                   if(cfg.disc2){
                      floppyCtrl.I8272out(port,(byte)value);
                   }
                   break;
            case 0xAC:
                 if (cfg.disc2) {
                    if ((value & 1) == 1) {
                        mem.mountDisc2();
                        floppyCtrl.dis2mnt = true;
                    }
                 }
                  break;
            case 0xEC:
                mem.SwitchAmos(value & 0x03);
                break;
            case 0xED:
                mem.SwitchAmos(value & 0x03);
                break;    
            case 0xEE:
                mem.SwitchAmos(value & 0x03);
                break;    
            case 0xEF:
                mem.SwitchAmos(value & 0x03);
                break;    
            case 0xD0:
                graf.D0 = value;
                break;
            case 0xD1:
                graf.D1 = value;
                break;
            case 0xD2:
                graf.wpD2 (value);
                break;
            case 0xD3:
                graf.wpD3(value);
                break;    
            case 0xD4:
                graf.wpD4(value);
                break; 
            case 0xF8:
                if (cfg.getSDRom()) {
                    sdrom.getPio().CpuWrite(sdrom.getPio().PP_PortA, value); 
                    sdrom.yield();
                }
                break;
            case 0xF9:
                if (cfg.getSDRom()) {
                    sdrom.getPio().CpuWrite(sdrom.getPio().PP_PortB, value);
                    sdrom.yield();
                }
                break;
            case 0xFA:
                if (cfg.getSDRom()) {
                    sdrom.getPio().CpuWrite(sdrom.getPio().PP_PortC, value);                    
                    sdrom.yield();
                }
                break;
            case 0xFB:
                if (cfg.getSDRom()) {
                    if (value == 180) {
                        //zmena smeru toku dat, musim pockat na dokonceni prace sdrom
                        while (sdrom.bSdromIn) {
                            sdrom.yield();
                        }
                    }
                    sdrom.getPio().CpuWrite(sdrom.getPio().PP_CWR, value);                                       
                    if (value == 129) {
                        //zmena smeru toku dat, musim pockat na dokonceni prace sdrom
                        while (!sdrom.bSdromIn) {
                            sdrom.yield();
                        }
                    }
                    //pokud prijde postupne 14 a pak 13, tak chce IQ ukladat bajt
                    if ((!bSav) && (value == 14)) {
                        bSav = true;
                    } else {
                        if ((bSav)&&(value == 13)) {
                            //ukladani z IQ do SDROM 
                            if (sdrom.itrInter==null){
                                sdrom.newInterrupt();
                                sdrom.startInterrupt();
                            }else{
                                if (sdrom.itrInter.isFinished()) {
                                 sdrom.newInterrupt();
                                 sdrom.startInterrupt(); 
                                }
                            }
                            
                        } else {
                            bSav = false;
                        }
                    }

                }
                break;

            case 0xFC:
            case 0xFD:
            case 0xFE:
            case 0xFF:
                if (cfg.getVideo()==cfg.VIDEO64){
                int aa=value&1;
                if (aa==0){cfg.V64ena32=false;}
                  else{cfg.V64ena32=true;}
                   }
                break;
            default:
                break;
        }
    }

    @Override
    public int atAddress(int address, int opcode) {
//        System.out.println(String.format("bp: %04X,%02X", address,opcode));
        return opcode;
    }

    @Override
    public boolean inSerial() {
        return false;
    }

    @Override
    public void outSerial(boolean sod) {
    }

    @Override
    public void execDone() {
    }

    //////////////////////////////////////////////////////////////////////
    
    @Override
    public void OnCpuWriteA() {
        
    }

    @Override
    public void OnCpuWriteB() {
        
    }

    @Override
    public void OnCpuWriteC() {
        if (pio==null) return;
        tapeo = pio.PeripheralReadBit(pio.PP_PortC, 0);        
        if (tapestart != pio.PeripheralReadBit(pio.PP_PortC, 1)) {
            tapestart = pio.PeripheralReadBit(pio.PP_PortC, 1);
            if (tapestart) { tap.tapeStart(); }
            else           { tap.tapeStop(); }
        }
    }

    @Override
    public void OnCpuWriteCL() {

    }
    
    @Override
    public void OnCpuWriteCH() {
        
    }

    @Override
    public void OnCpuWriteCWR(int value) {
        
    }

    @Override
    public void OnCpuReadA() {
        pio.PeripheralWriteByte(pio.PP_PortA, 
         key.readKeyboardPortA(pio.PeripheralReadByte(pio.PP_PortB)));
    }

    @Override
    public void OnCpuReadB() {
        pio.PeripheralWriteByte(pio.PP_PortB, 
         key.readKeyboardPortB(pio.PeripheralReadByte(pio.PP_PortA)));      
    }

    @Override
    public void OnCpuReadC() {
        
    }

    @Override
    public void OnCpuReadCL() {
        
    }

    @Override
    public void OnCpuReadCH() {
        if (tapestart) {
            pio.PeripheralChangeBit(pio.PP_PortC, 7, tapein);            
            pio.PeripheralChangeBit(pio.PP_PortC, 6, false);            
            pio.PeripheralChangeBit(pio.PP_PortC, 5, khz1);            
            pio.PeripheralChangeBit(pio.PP_PortC, 4, false);            
        }
        else {
            pio.PeripheralChangeBit(pio.PP_PortC, 7, !key.isFB());
            pio.PeripheralChangeBit(pio.PP_PortC, 6, !key.isFA());
            pio.PeripheralChangeBit(pio.PP_PortC, 5, !key.isCtrl());
            pio.PeripheralChangeBit(pio.PP_PortC, 4, !key.isShift());
        }
    }

    void setTapeIn(boolean readSample) {
        tapein = readSample ^ tapeinv;
    }

    boolean getTapeOut() {
        return tapeout ^ khz1;
    }

    public void openLoadTape(String canonicalPath) {
        tap.openLoadTape(canonicalPath);
    }

    public void openSaveTape(String canonicalPath) {
        tap.openSaveTape(canonicalPath);
    }

    public void setTapeMode(boolean record) {
        tap.setTapeMode(record);
    }

    public void shutdownCleanup() {
        tap.closeCleanup();
    }

    public void setTapeInvert(boolean inverted) {
        tapeinv = inverted;
    }

    // ========================================================================
    //  Snapshoty .isn (signatura "ISN") - obdoba .osn z emulatoru Ondry.
    //
    //  Uklada se: CPU i8080/8085, cela RAM (64 KB), VRAM, stav prepinani
    //  pameti, 8255, 8259, latch portu 0x80, magnetofonove priznaky a modul
    //  Grafik. Neuklada se SD-ROM (emulovana ATmega ve vlastnim vlakne),
    //  radic Disc2 s obrazy disket, poloha pasky ani zvukovy buffer.
    //
    //  T-stavy ani nSpeed se neukladaji - jsou to volne bezici citac,
    //  resp. uzivatelska preference okna, ne stav stroje.
    // ========================================================================

    private static final int ISN_VERSION = 1;

    private String strLastSnapshotError = "";

    public String getLastSnapshotError() {
        return strLastSnapshotError;
    }

    /** Blok konfigurace modulu, ktery se uklada do hlavicky snapshotu. */
    private byte[] snapshotConfigBlock() {
        return new byte[] {
            cfg.getMain(),
            cfg.getMonitor(),
            cfg.getVideo(),
            (byte) (cfg.getMem64() ? 1 : 0),
            (byte) (cfg.getGrafik() ? 1 : 0),
            (byte) (cfg.getSDRom() ? 1 : 0),
            (byte) (cfg.getDisc2() ? 1 : 0),
            (byte) (cfg.getAudio() ? 1 : 0),
            (byte) (cfg.getSmartKbd() ? 1 : 0),
            (byte) (cfg.V64ena32 ? 1 : 0)
        };
    }

    private static String mainModuleName(int b) {
        switch (b) {
            case 0:  return "žádný";
            case 1:  return "BASIC6";
            case 2:  return "BASIC G";
            case 3:  return "AMOS";
            default: return "?";
        }
    }

    private static String monitorName(int b) {
        switch (b) {
            case 10: return "standardní";
            case 11: return "disassembler";
            case 12: return "CP/M Komenium";
            case 13: return "CP/M Felicie";
            default: return "?";
        }
    }

    private static void diff(StringBuilder sb, String what, String inFile, String current) {
        if (!inFile.equals(current)) {
            sb.append("    ").append(what).append(": snapshot = ").append(inFile)
              .append(", nastaveno = ").append(current).append('\n');
        }
    }

    private static String onOff(int b) {
        return b != 0 ? "zapnuto" : "vypnuto";
    }

    /** Porovna konfiguraci ze snapshotu s aktualni. Vraci null pri shode,
     *  jinak citelny seznam rozdilu pro uzivatele. */
    private String checkSnapshotConfig(byte[] fileCfg) {
        byte[] cur = snapshotConfigBlock();
        StringBuilder sb = new StringBuilder();

        diff(sb, "Hlavní modul", mainModuleName(fileCfg[0]), mainModuleName(cur[0]));
        diff(sb, "Monitor", monitorName(fileCfg[1]), monitorName(cur[1]));
        diff(sb, "Video", fileCfg[2] == cfg.VIDEO64 ? "64 znaků" : "32 znaků",
                          cur[2] == cfg.VIDEO64 ? "64 znaků" : "32 znaků");
        diff(sb, "Paměť 64 KB", onOff(fileCfg[3]), onOff(cur[3]));
        diff(sb, "Grafik", onOff(fileCfg[4]), onOff(cur[4]));
        diff(sb, "SD-ROM", onOff(fileCfg[5]), onOff(cur[5]));
        diff(sb, "Disc2", onOff(fileCfg[6]), onOff(cur[6]));
        diff(sb, "Zvuk", onOff(fileCfg[7]), onOff(cur[7]));
        diff(sb, "Zdvojování na 32 znaků", onOff(fileCfg[9]), onOff(cur[9]));

        return sb.length() == 0 ? null : sb.toString();
    }

    private static void write16(BufferedOutputStream fOut, int word) throws IOException {
        fOut.write(word & 0xff);
        fOut.write((word >>> 8) & 0xff);
    }

    private static int read8(BufferedInputStream fIn) throws IOException {
        int b = fIn.read();
        if (b < 0) {
            throw new EOFException("Neočekávaný konec snapshotu");
        }
        return b;
    }

    private static int read16(BufferedInputStream fIn) throws IOException {
        int lo = read8(fIn);
        return lo | (read8(fIn) << 8);
    }

    // ---- rychly slot (Ctrl+F8 / Ctrl+F9) ---------------------------------------
    //
    // Jeden pevne dany soubor v adresari pro docasne soubory. Prezije
    // vypnuti a zapnuti emulatoru, restart pocitace uz nutne ne - coz je
    // presne to, co se od rychleho slotu ceka.

    private String strQuickSlotPath = null;

    public String getQuickSlotPath() {
        if (strQuickSlotPath == null) {
            String tmp = System.getProperty("java.io.tmpdir");
            if (tmp == null || tmp.isEmpty()) {
                tmp = ".";
            }
            if (!tmp.endsWith(File.separator)) {
                tmp += File.separator;
            }
            strQuickSlotPath = tmp + "jiq151-quickslot.isn";
        }
        return strQuickSlotPath;
    }

    /** Rychle ulozeni do pevneho slotu; predchozi obsah se prepise. */
    public boolean quickSave() {
        return saveSnapshot(getQuickSlotPath());
    }

    /** Rychle nacteni z pevneho slotu. */
    public boolean quickLoad() {
        if (!new File(getQuickSlotPath()).exists()) {
            strLastSnapshotError = "Rychlý slot je zatím prázdný – nejdřív uložte stav klávesami Ctrl+F8.";
            return false;
        }
        return loadSnapshot(getQuickSlotPath());
    }

    /** Ulozi kompletni stav stroje do souboru .isn.
     *  @return true pri uspechu, jinak viz getLastSnapshotError() */
    public boolean saveSnapshot(String filename) {
        strLastSnapshotError = "";
        BufferedOutputStream fOut = null;
        try {
            fOut = new BufferedOutputStream(new FileOutputStream(filename));

            // signatura + verze
            fOut.write('I');
            fOut.write('S');
            fOut.write('N');
            fOut.write(ISN_VERSION);

            // konfigurace modulu
            fOut.write(snapshotConfigBlock());

            // CPU i8080/8085
            int af = cpu.getRegAF();
            fOut.write((af >>> 8) & 0xff);          // A
            fOut.write(af & 0xff);                  // F
            fOut.write(cpu.getRegB());
            fOut.write(cpu.getRegC());
            fOut.write(cpu.getRegD());
            fOut.write(cpu.getRegE());
            fOut.write(cpu.getRegH());
            fOut.write(cpu.getRegL());
            write16(fOut, cpu.getRegSP());
            write16(fOut, cpu.getRegPC());
            write16(fOut, cpu.getMemPtr());
            fOut.write(cpu.getRegSim());
            int cpuFlags = 0;
            if (cpu.isPendingEI())  { cpuFlags |= 1; }
            if (cpu.isActiveInt())  { cpuFlags |= 2; }
            if (cpu.isHalted())     { cpuFlags |= 4; }
            fOut.write(cpuFlags);

            // stav stroje a prepinani pameti
            fOut.write(port80 & 0xff);
            fOut.write(mem.isBootstrap() ? 1 : 0);
            fOut.write(mem.getAmosBank() < 0 ? 0xff : mem.getAmosBank());
            fOut.write(mem.isDisc2Mounted() ? 1 : 0);
            int tapeFlags = 0;
            if (khz1)      { tapeFlags |= 1; }
            if (tapein)    { tapeFlags |= 2; }
            if (tapeo)     { tapeFlags |= 4; }
            if (tapeout)   { tapeFlags |= 8; }
            if (tapestart) { tapeFlags |= 16; }
            if (tapeinv)   { tapeFlags |= 32; }
            fOut.write(tapeFlags);
            fOut.write(0);                          // rezerva

            // 8255 (CWR, PC, PB, PA, preruseni)
            int[] pioState = pio.getState();
            for (int i = 0; i < 5; i++) {
                fOut.write(pioState[i] & 0xff);
            }

            // 8259
            PicState ps = ic.getPicSate();
            fOut.write(ps.icw1 & 0xff);
            fOut.write(ps.icw2 & 0xff);
            fOut.write(ps.icw3 & 0xff);
            fOut.write(ps.icw4 & 0xff);
            fOut.write(ps.ocw1 & 0xff);
            fOut.write(ps.ocw2 & 0xff);
            fOut.write(ps.ocw3 & 0xff);
            fOut.write(ps.irr & 0xff);
            fOut.write(ps.isr & 0xff);
            fOut.write(ps.state.ordinal());
            fOut.write(ps.intack.ordinal());

            // Grafik
            if (cfg.grafik) {
                fOut.write(1);
                fOut.write(graf.D0 & 0xff);
                fOut.write(graf.D1 & 0xff);
                fOut.write(graf.D2 & 0xff);
                int gFlags = 0;
                if (graf.Enabled)   { gFlags |= 1; }
                if (graf.ShowGR)    { gFlags |= 2; }
                if (graf.BitAcces)  { gFlags |= 4; }
                if (graf.PenOn)     { gFlags |= 8; }
                fOut.write(gFlags);
                graf.saveSnapshot(fOut);
            } else {
                fOut.write(0);
            }

            // pamet
            mem.saveSnapshotRam(fOut);
            mem.saveSnapshotVRam(fOut);

            fOut.close();
            fOut = null;
            return true;
        } catch (IOException ex) {
            strLastSnapshotError = "Snapshot se nepodařilo uložit: " + ex.getMessage();
            Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (fOut != null) {
                try { fOut.close(); } catch (IOException ex) { }
            }
        }
    }

    /** Nacte stav stroje ze souboru .isn. Pri chybne signature nebo neshode
     *  konfigurace modulu se stroj vubec nezmeni.
     *  @return true pri uspechu, jinak viz getLastSnapshotError() */
    public boolean loadSnapshot(String filename) {
        strLastSnapshotError = "";
        BufferedInputStream fIn = null;
        try {
            fIn = new BufferedInputStream(new FileInputStream(filename));

            // signatura se na rozdil od .osn skutecne kontroluje
            if (read8(fIn) != 'I' || read8(fIn) != 'S' || read8(fIn) != 'N') {
                strLastSnapshotError = "Toto není snapshot IQ 151 (chybí signatura ISN).";
                return false;
            }
            int version = read8(fIn);
            if (version != ISN_VERSION) {
                strLastSnapshotError = "Nepodporovaná verze snapshotu: " + version
                        + " (tato verze emulátoru umí " + ISN_VERSION + ").";
                return false;
            }

            byte[] fileCfg = new byte[10];
            for (int i = 0; i < fileCfg.length; i++) {
                fileCfg[i] = (byte) read8(fIn);
            }
            String rozdily = checkSnapshotConfig(fileCfg);
            if (rozdily != null) {
                strLastSnapshotError = "Snapshot byl uložen s jinou konfigurací modulů.\n"
                        + "Nastavte v Tools → Settings:\n" + rozdily;
                return false;
            }

            // od tohoto mista uz se stroj meni
            mem.Reset(false);       // prebuduje tabulky stranek, RAM nemaze

            // CPU
            int a = read8(fIn);
            int f = read8(fIn);
            cpu.setRegAF((a << 8) | f);
            cpu.setRegB(read8(fIn));
            cpu.setRegC(read8(fIn));
            cpu.setRegD(read8(fIn));
            cpu.setRegE(read8(fIn));
            cpu.setRegH(read8(fIn));
            cpu.setRegL(read8(fIn));
            cpu.setRegSP(read16(fIn));
            cpu.setRegPC(read16(fIn));
            cpu.setMemPtr(read16(fIn));
            cpu.setRegSim(read8(fIn));
            int cpuFlags = read8(fIn);
            cpu.setPendingEI((cpuFlags & 1) != 0);
            cpu.setActiveInt((cpuFlags & 2) != 0);
            cpu.setHalted((cpuFlags & 4) != 0);

            // stav stroje a prepinani pameti
            port80 = read8(fIn);
            boolean bootstrap = read8(fIn) != 0;
            int amosBank = read8(fIn);
            boolean disc2Mounted = read8(fIn) != 0;
            int tapeFlags = read8(fIn);
            khz1      = (tapeFlags & 1) != 0;
            tapein    = (tapeFlags & 2) != 0;
            tapeo     = (tapeFlags & 4) != 0;
            tapeout   = (tapeFlags & 8) != 0;
            tapestart = (tapeFlags & 16) != 0;
            tapeinv   = (tapeFlags & 32) != 0;
            read8(fIn);                             // rezerva

            // 8255
            int[] pioState = new int[5];
            for (int i = 0; i < 5; i++) {
                pioState[i] = read8(fIn);
            }
            pio.setState(pioState);

            // 8259
            PicState ps = new PicState();
            ps.icw1 = read8(fIn);
            ps.icw2 = read8(fIn);
            ps.icw3 = read8(fIn);
            ps.icw4 = read8(fIn);
            ps.ocw1 = read8(fIn);
            ps.ocw2 = read8(fIn);
            ps.ocw3 = read8(fIn);
            ps.irr  = read8(fIn);
            ps.isr  = read8(fIn);
            ps.state  = Pic.init.values()[read8(fIn) % Pic.init.values().length];
            ps.intack = Pic.iack.values()[read8(fIn) % Pic.iack.values().length];
            ic.setPicSate(ps);

            // prepnuti pameti az po mem.Reset()
            mem.setBootstrap(bootstrap);
            if (amosBank != 0xff) {
                mem.SwitchAmos(amosBank);
            }
            if (disc2Mounted) {
                mem.mountDisc2();
                if (floppyCtrl != null) {
                    floppyCtrl.dis2mnt = true;
                }
            }

            // Grafik
            if (read8(fIn) != 0) {
                graf.D0 = read8(fIn);
                graf.D1 = read8(fIn);
                graf.D2 = read8(fIn);
                int gFlags = read8(fIn);
                graf.Enabled  = (gFlags & 1) != 0;
                graf.ShowGR   = (gFlags & 2) != 0;
                graf.BitAcces = (gFlags & 4) != 0;
                graf.PenOn    = (gFlags & 8) != 0;
                graf.loadSnapshot(fIn);
            }

            // pamet
            mem.loadSnapshotRam(fIn);
            mem.loadSnapshotVRam(fIn);

            fIn.close();
            fIn = null;

            key.Reset();            // aby po nacteni neuvizla klavesa
            redrawScreen();
            return true;
        } catch (IOException ex) {
            if (strLastSnapshotError.isEmpty()) {
                strLastSnapshotError = "Snapshot se nepodařilo načíst: " + ex.getMessage();
            }
            Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (fIn != null) {
                try { fIn.close(); } catch (IOException ex) { }
            }
        }
    }
}
