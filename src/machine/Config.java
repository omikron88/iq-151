/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

/**
 *
 * @author Administrator
 */
public class Config {
    public final byte VIDEO32 = 0;
    public final byte VIDEO64 = 1;

    public boolean grafik = true;
    public boolean sdrom = false;
    public boolean audio = false;
    public boolean V64ena32=false;
   
    public final byte NONE = 0;
    public final byte BASIC6 = 1;
    public final byte BASICG = 2;
    public final byte AMOS = 3;

    
    public final byte Mstandard = 10;
    public final byte Mdisassembler = 11;
    public final byte MCPMkom = 12;
    public final byte MCPMfel = 13;
  
    private byte video = VIDEO32; 
    private byte main = BASICG;
    private byte monit = Mstandard;
    private boolean mem64 = false;
    
    private String roms = "roms/";
    private String charsRom = "chars.bin";
    private String monStdRom = "Monitor-std.rom";
    private String monDisRom = "Monitor-dis.rom";
    private String monKomRom = "Monitor-cpm-Variel.rom";
    private String monFelRom = "Monitor-cpm-030188.rom";
    private String basic6Rom = "Basic6.rom";
    private String basicGRom = "BasicG.rom";
    private String basicG2Rom = "BasicG2.rom";
    private String assemblerRom = "Assembler.rom";
    private String pascalRom = "Pascal.rom";
    private String pascal1Rom = "Pascal1.rom";



    public byte getVideo() {
        return video;
    }
    
    public void setVideo(byte b) {
        if (b!=video) {
            video = b;
        }
    }

    public byte getMain() {
        return main;
    }
    
    public void setMain(byte b) {
        if (b!=main) {
            main = b;
        }
    }

    public boolean getGrafik() {
        return grafik;
    }
    
    public void setGrafik(boolean b) {
        if (b!=grafik) {
            grafik = b;
        }
    }
    
    public boolean getSDRom() {
        return sdrom;
    }
    
    public void setSDRom(boolean b) {
        if (b!=sdrom) {
            sdrom = b;
        }
    }
    
    public boolean getAudio() {
        return audio;
    }
    
    public void setAudio(boolean b) {
        if (b!=audio) {
            audio = b;
        }
    }

    public byte getMonitor() {
        return monit;
    }
    
   
    public void setMonitor(byte b) {
        if (b!=monit) {
            monit = b;
        }
    }
    


    public boolean getMem64() {
        return mem64;
    }
    
    public void setMem64(boolean b) {
        if (b!=mem64) {
            mem64 = b;
        }
    }
    
    public String getRomsDirectory() {
        return roms;
    }
    
    public String getCharsRom() {
        return charsRom;
    }
    
    public String getMonStdRom() {
        return monStdRom;
    }
    
    public String getMonDisRom() {
        return monDisRom;
    }
    public String getMonKomRom() {
        return monKomRom;
    }
    public String getMonFelRom() {
        return monFelRom;
    }
    public String getBasic6Rom() {
        return basic6Rom;
    }

   public String getBasicGRom() {
        return basic6Rom;
    }
   public String getBasicG2Rom() {
        return basicG2Rom;
    }
   public String getAssemblerRom() {
        return assemblerRom;
    }
   public String getPascalRom() {
        return pascalRom;
    }
   public String getPascal1Rom() {
        return pascal1Rom;
    }
}
