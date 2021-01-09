/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Memory {

    public final int PAGE_SIZE = 1024;
    public final int PAGE_MASK = PAGE_SIZE - 1;
    public final byte PAGE_BIT = 10;

    private byte[][] Ram = new byte[64][PAGE_SIZE];
    private byte[][] VRam = new byte[2][PAGE_SIZE];
    private byte[][] Chars = new byte[1][PAGE_SIZE];
    private byte[][] Monitor = new byte[4][PAGE_SIZE];
    private byte[][] Basic6 = new byte[8][PAGE_SIZE];
    private byte[][] BasicG = new byte[8][PAGE_SIZE];
    private byte[][] BasicG2 = new byte[4][PAGE_SIZE];

    private byte[][] readPages = new byte[64][];
    private byte[][] writePages = new byte[64][];
    private byte[] fakeROM = new byte[PAGE_SIZE]; 
    private byte[] fakeRAM = new byte[PAGE_SIZE];
 
    private Config cf;
    public Memory(Config cnf) {
        cf = cnf;
        loadRoms();
    }
    
    public void Reset(boolean dirty) {
        if (dirty) {
            char c = 0;
            int a = 0;
            for(int i=0; i<64; i++) {
                for(int j=0; j<PAGE_SIZE; j++) {
                    Ram[i][j] = (byte) c;
                    a = a++ & 127;
                    if (a==0) { c ^= 255; };
                }
            }
        }
        
        for(int i=0; i<PAGE_SIZE; i++) {
            fakeRAM[i] = (byte) 255;
        }
        
        int er = cf.getMem64() ? 64:32;
        
        for(int i=0; i<63; i++) {
            if (i<er) {
                readPages[i] = writePages[i] = Ram[i];   
            }
            else {
                readPages[i] = fakeRAM;
                writePages[i] = fakeROM;
            }   
        }
        
        // Monitor        
        readPages[63] = Monitor[3];
        writePages[63] = fakeROM;
        readPages[62] = Monitor[2];
        writePages[62] = fakeROM;
        readPages[61] = Monitor[1];
        writePages[61] = fakeROM;
        readPages[60] = Monitor[0];
        writePages[60] = fakeROM;

        // VRam
        if (cf.getVideo()==cf.VIDEO32) { // 32
            readPages[59] = writePages[59] = VRam[0];
        }
        else {                  // 64
            readPages[59] = writePages[59] = VRam[1];
            readPages[58] = writePages[58] = VRam[0];
        }
        
        // main module
        if (cf.getMain()==cf.BASIC6) { // Basic6
            readPages[57] = Basic6[7];
            writePages[57] = fakeROM;
            readPages[56] = Basic6[6];
            writePages[56] = fakeROM;
            readPages[55] = Basic6[5];
            writePages[55] = fakeROM;
            readPages[54] = Basic6[4];
            writePages[54] = fakeROM;
            readPages[53] = Basic6[3];
            writePages[53] = fakeROM;
            readPages[52] = Basic6[2];
            writePages[52] = fakeROM;
            readPages[51] = Basic6[1];
            writePages[51] = fakeROM;
            readPages[50] = Basic6[0];
            writePages[50] = fakeROM;
        }
        
        if (cf.getMain()==cf.BASICG) { // BasicG
            readPages[57] = BasicG[7];
            writePages[57] = fakeROM;
            readPages[56] = BasicG[6];
            writePages[56] = fakeROM;
            readPages[55] = BasicG[5];
            writePages[55] = fakeROM;
            readPages[54] = BasicG[4];
            writePages[54] = fakeROM;
            readPages[53] = BasicG[3];
            writePages[53] = fakeROM;
            readPages[52] = BasicG[2];
            writePages[52] = fakeROM;
            readPages[51] = BasicG[1];
            writePages[51] = fakeROM;
            readPages[50] = BasicG[0];
            writePages[50] = fakeROM;
            readPages[47] = BasicG2[3];
            writePages[47] = fakeROM;
            readPages[46] = BasicG2[2];
            writePages[46] = fakeROM;
            readPages[45] = BasicG2[1];
            writePages[45] = fakeROM;
            readPages[44] = BasicG2[0];
            writePages[44] = fakeROM;
        }
    }  
    
    public void setBootstrap(boolean b) {
        if (b) {
            readPages[0] = Monitor[2];
            writePages[0] = fakeROM;
            readPages[1] = Monitor[3];
            writePages[1] = fakeROM;
        }
        else {
            readPages[0] = writePages[0] = Ram[0];   
            readPages[1] = writePages[1] = Ram[1];   
        }
            
    }
    
    public byte readByte(int address) {
        return readPages[address >>> PAGE_BIT][address & PAGE_MASK];
    }
    
    public void writeByte(int address, byte value) {
        writePages[address >>> PAGE_BIT][address & PAGE_MASK] = value;
    }
    
    public byte[][] getVRam() {
        return VRam;
    }
    
    public byte[] getChars() {
        return Chars[0];
    }
    
    private void loadRoms() {
        String romsDirectory = cf.getRomsDirectory();

        if (!romsDirectory.isEmpty() && !romsDirectory.endsWith("/")) {
            romsDirectory += "/";
        }

        if (!loadRomAsFile(romsDirectory + cf.getCharsRom(), Chars, 0, PAGE_SIZE * 1)) {
            loadRomAsResource("/roms/chars.bin", Chars, 0, PAGE_SIZE * 1);
        }
        if (!loadRomAsFile(romsDirectory + cf.getMonitorRom(), Monitor, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/Monitor.rom", Monitor, 0, PAGE_SIZE * 4);
        }
        if (!loadRomAsFile(romsDirectory + cf.getBasic6Rom(), Monitor, 0, PAGE_SIZE * 8)) {
            loadRomAsResource("/roms/Basic6.rom", Basic6, 0, PAGE_SIZE * 8);
        }        
        if (!loadRomAsFile(romsDirectory + cf.getBasicGRom(), BasicG, 0, PAGE_SIZE * 8)) {
            loadRomAsResource("/roms/BasicG.rom", BasicG, 0, PAGE_SIZE * 8);
        }        
        if (!loadRomAsFile(romsDirectory + cf.getBasic6Rom(), BasicG2, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/BasicG2.rom", BasicG2, 0, PAGE_SIZE * 4);
        }
    }

    private boolean loadRomAsResource(String filename, byte[][] rom, int page, int size) {

        InputStream inRom = Iq.class.getResourceAsStream(filename);
        boolean res = false;

        if (inRom == null) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "RESOURCE_ROM_ERROR");
            System.out.println(String.format("%s: %s", msg, filename));
            return false;
        }

        try {
            for (int frag = 0; frag < size / PAGE_SIZE; frag++) {
                int count = 0;
                while (count != -1 && count < PAGE_SIZE) {
                    count += inRom.read(rom[page + frag], count, PAGE_SIZE - count);
                }

                if (count != PAGE_SIZE) {
                    String msg =
                        java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                        "ROM_SIZE_ERROR");
                    System.out.println(String.format("%s: %s", msg, filename));
                } else {
                    res = true;
                }
            }
        } catch (IOException ex) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "RESOURCE_ROM_ERROR");
            System.out.println(String.format("%s: %s", msg, filename));
            Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inRom.close();
            } catch (IOException ex) {
                Logger.getLogger(Memory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (res) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "ROM_RESOURCE_LOADED");
            System.out.println(String.format("%s: %s", msg, filename));
        }

        return res;
    }

    private boolean loadRomAsFile(String filename, byte[][] rom, int page, int size) {
        BufferedInputStream fIn = null;
        boolean res = false;

        try {
            try {
                fIn = new BufferedInputStream(new FileInputStream(filename));
            } catch (FileNotFoundException ex) {
                String msg =
                    java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                    "FILE_ROM_ERROR");
                System.out.println(String.format("%s: %s", msg, filename));
                //Logger.getLogger(Spectrum.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            for (int frag = 0; frag < size / PAGE_SIZE; frag++) {
                int count = 0;
                while (count != -1 && count < PAGE_SIZE) {
                    count += fIn.read(rom[page + frag], count, PAGE_SIZE - count);
                }

                if (count != PAGE_SIZE) {
                    String msg =
                        java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                        "ROM_SIZE_ERROR");
                    System.out.println(String.format("%s: %s", msg, filename));
                } else {
                    res = true;
                }
            }
        } catch (IOException ex) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "FILE_ROM_ERROR");
            System.out.println(String.format("%s: %s", msg, filename));
            Logger.getLogger(Iq.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fIn != null) {
                    fIn.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Memory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (res) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "ROM_FILE_LOADED");
            System.out.println(String.format("%s: %s", msg, filename));
        }

        return res;
    }

}