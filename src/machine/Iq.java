/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import gui.JIQScreen;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import gui.Debugger;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;


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
    private Debugger deb;
    
    public FileInputStream reader=null;
    boolean bReadyToSend=false;
    public FileOutputStream writer=null;
    boolean bReadyToWrite=false;
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
    
    public Iq() {                
        img = new BufferedImage(sirka, vyska, BufferedImage.TYPE_INT_RGB);
     //   px = ((DataBufferByte) img.getRaster().getDataBuffer()).getBankData()[0];
        try {
             reader = new FileInputStream(utils.Config.getMyPath()+"tapein.txt");            
        } catch (FileNotFoundException ex) {
            reader=null;
        }
        cfg = new Config();
        utils.Config.LoadConfig();
        cfg.setMain((byte)utils.Config.mainmodule);
        cfg.setGrafik(utils.Config.grafik);
        cfg.setMem64(utils.Config.mem64);
        cfg.setVideo((byte)utils.Config.video64);
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
        
        tap = new Tape(this);
        
        paused = true;
        Reset(true);
    }
    
     public void setDebugger(Debugger indeb){
        deb=indeb;
    }
    
    public Debugger getDebugger(){
        return deb;
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
        mem.Reset(dirty);
        port80 = 0;
        mem.setBootstrap(true);
        clk.reset();
        ic.Reset();
        cpu.reset();
        pio.Reset();
        key.Reset();
        // pro zrychlení vykreslování se potřebuju zbavit BYTE u chars
        // a připravit tabulky s adresama
        for(int i=0; i<1024; i++) {znsada[i]=(chars[i]&255);
                                   znsadainv[i]=255-znsada[i];
                                   dstg[i]=2*(i & 0x1f)+((i>>5)*512);
                                   dstg64[i]=(i & 0x3f)+((i>>6)*512);
                                   dstg64[i+1024]=dstg64[i]+8192;
                                  }
        
//        cpu.setBreakpoint(0xf76b, true);
    }
    
    public synchronized void startEmulation() {
        if (!paused)
            return;
        
        paused = false;
        task = new IqTimer(this);
        tim.scheduleAtFixedRate(task, 100, 20);
       }
    
    public synchronized void stopEmulation() {
        if (paused)
            return;
        
        paused = true;
        task.cancel();
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public void ms20() {
        if (paused) 
            return;
        
        ic.assertInt(6);
        for(int t=0; t<40; t++) {               // half period of 1kHz
            if (!paused){
             cpu.execute(clk.getTstates()+1024); // is 1024 of 2MHz Tstates
             khz1 = !khz1;
             if (khz1) tapeout = tapeo;
            }
        }
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
         //vymazat případné zdvojené znaky za řádkem
         if ((adl&63)==63){for (int kk=0;kk<8;kk++){img.setRGB(a++, b, col0);}}
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
        clk.addTstates(4);
        port &= 0xff;
        if(port==0xFA){
         bReadyToSend=true;
         return 255; //pin 0 - ctecka ready pro zapis znaku do IQ
         //return 8; //pin 3 - IQ ready pro prijmuti znaku
        }
        if (port == 0xF8) {
            if (bReadyToSend) {
                int ch;
                try {
                    if ((ch = reader.read()) != -1) {
                        lastchar=ch;
                        return ch;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        /*
        String strINfile= utils.Config.getMyPath()+"in.txt";
        try {
            PrintWriter f = new PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(strINfile,true)));
            f.println(String.format("In: %02X (%04X)", port,cpu.getRegPC()));
            f.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
        //System.out.println(String.format("In: %02X (%04X)", port,cpu.getRegPC()));
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
            case 0xD4:
                return graf.rpD4();
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
        if (port == 0xFB) {
          int deb=0;
          deb++;
          } 
        if (port == 0xF9) {
            if (value != 0) {
                int nVal = 127 & value;
                try {
                    if (writer == null) {
                        try {
                            writer = new FileOutputStream(utils.Config.getMyPath() + "tapeout.txt");
                            writer.write(nVal);
                        } catch (FileNotFoundException ex) {
                            writer = null;
                        }
                    } else {

                        writer.write(nVal);

                    }
                } catch (IOException ex) {
                    Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

         
        /*
        String strINfile= utils.Config.getMyPath()+"out.txt";
        try {
            PrintWriter f = new PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(strINfile,true)));
            f.println(String.format("Out: %02X,%02X (%04X)", port,value,cpu.getRegPC()));
            f.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        //System.out.println(String.format("Out: %02X,%02X (%04X)", port,value,cpu.getRegPC()));
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
                break;
            case 0x87:
                pio.CpuWrite(pio.PP_CWR, value);
                break;
            case 0x88:
                ic.writePortA0(value);
                break;
            case 0x89:
                ic.writePortA1(value);
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
}
