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
    public boolean V64ena32=false;
   
    public final byte NONE = 0;
    public final byte BASIC6 = 1;
    public final byte BASICG = 2;
    
    private byte video = VIDEO32; 
    private byte main = BASICG;
    private boolean mem64 = false;
    
    private String roms = "roms/";
    private String charsRom = "chars.bin";
    private String monitorRom = "Monitor.rom";
    private String basic6Rom = "Basic6.rom";
    private String basicGRom = "BasicG.rom";
    private String basicG2Rom = "BasicG2.rom";

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
    
    public String getMonitorRom() {
        return monitorRom;
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
}
