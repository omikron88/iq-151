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
    private byte[][] MonStd = new byte[4][PAGE_SIZE];
    private byte[][] MonDis = new byte[4][PAGE_SIZE];
    private byte[][] MonKom = new byte[4][PAGE_SIZE];
    private byte[][] MonFel = new byte[4][PAGE_SIZE];
    private byte[][] Basic6 = new byte[8][PAGE_SIZE];
    private byte[][] BasicG = new byte[8][PAGE_SIZE];
    private byte[][] BasicG2 = new byte[4][PAGE_SIZE];
    private byte[][] Pascal = new byte[16][PAGE_SIZE];
    private byte[][] Pascal1 = new byte[16][PAGE_SIZE];
    private byte[][] Assembler = new byte[16][PAGE_SIZE];
    private byte[][] Disc2 = new byte[2][PAGE_SIZE];

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
            char c = 255;  // bylo 0
            int a = 0;
            for(int i=0; i<64; i++) {
                for(int j=0; j<PAGE_SIZE; j++) {
                    Ram[i][j] = (byte) c;
                //    a = a++ & 127;
                //    if (a==0) { c ^= 255; };
                }
            }
        }
        
        for(int i=0; i<PAGE_SIZE; i++) {
            fakeRAM[i] = (byte) 255;
        }
        
        int er = cf.getMem64() ? 64:32;
        
        for(int i=0; i<60; i++) {
            if (i<er) {
                readPages[i] = writePages[i] = Ram[i];   
            }
            else {
                readPages[i] = fakeRAM;
                writePages[i] = fakeROM;
            }   
        }
        
        // Monitor 
        if (cf.getMonitor()==cf.Mstandard) { 
           Monitor[0] = MonStd[0]; 
           Monitor[1] = MonStd[1]; 
           Monitor[2] = MonStd[2]; 
           Monitor[3] = MonStd[3];
        }
        if (cf.getMonitor()==cf.Mdisassembler) { 
           Monitor[0] = MonDis[0]; 
           Monitor[1] = MonDis[1]; 
           Monitor[2] = MonDis[2]; 
           Monitor[3] = MonDis[3];
        }
         if (cf.getMonitor()==cf.MCPMkom) { 
           Monitor[0] = MonKom[0]; 
           Monitor[1] = MonKom[1]; 
           Monitor[2] = MonKom[2]; 
           Monitor[3] = MonKom[3];
        }
          if (cf.getMonitor()==cf.MCPMfel) { 
           Monitor[0] = MonFel[0]; 
           Monitor[1] = MonFel[1]; 
           Monitor[2] = MonFel[2]; 
           Monitor[3] = MonFel[3];
        }
        
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
        if (cf.getMain()==cf.AMOS) { // Amos - Assembler jako výchozí
            readPages[47] = Assembler[15];
            writePages[47] = fakeROM;
            readPages[46] = Assembler[14];
            writePages[46] = fakeROM;
            readPages[45] = Assembler[13];
            writePages[45] = fakeROM;
            readPages[44] = Assembler[12];
            writePages[44] = fakeROM;
            readPages[43] = Assembler[11];
            writePages[43] = fakeROM;
            readPages[42] = Assembler[10];
            writePages[42] = fakeROM;
            readPages[41] = Assembler[9];
            writePages[41] = fakeROM;
            readPages[40] = Assembler[8];
            writePages[40] = fakeROM;
            readPages[39] = Assembler[7];
            writePages[39] = fakeROM;
            readPages[38] = Assembler[6];
            writePages[38] = fakeROM;
            readPages[37] = Assembler[5];
            writePages[37] = fakeROM;
            readPages[36] = Assembler[4];
            writePages[36] = fakeROM;
            readPages[35] = Assembler[3];
            writePages[35] = fakeROM;
            readPages[34] = Assembler[2];
            writePages[34] = fakeROM;
            readPages[33] = Assembler[1];
            writePages[33] = fakeROM;
            readPages[32] = Assembler[0];
            writePages[32] = fakeROM;
        }
    }  
    
    public void setBootstrap(boolean b) {
        if (b) {
            readPages[0] = Monitor[2];
            writePages[0] = Ram[0];
            readPages[1] = Monitor[3];
            writePages[1] = Ram[1];
        }
        else {
            readPages[0] = writePages[0] = Ram[0];   
            readPages[1] = writePages[1] = Ram[1];   
        }
            
    }
    
    public void mountDisc2() {
        readPages[57] = Disc2[1];
        writePages[57] = fakeROM;
        readPages[56] = Disc2[0];
        writePages[56] = fakeROM;
    }
    
    public void SwitchAmos(int hodn) {
        if (hodn == 0) {
            readPages[47] = Pascal[15];
            writePages[47] = fakeROM;
            readPages[46] = Pascal[14];
            writePages[46] = fakeROM;
            readPages[45] = Pascal[13];
            writePages[45] = fakeROM;
            readPages[44] = Pascal[12];
            writePages[44] = fakeROM;
            readPages[43] = Pascal[11];
            writePages[43] = fakeROM;
            readPages[42] = Pascal[10];
            writePages[42] = fakeROM;
            readPages[41] = Pascal[9];
            writePages[41] = fakeROM;
            readPages[40] = Pascal[8];
            writePages[40] = fakeROM;
            readPages[39] = Pascal[7];
            writePages[39] = fakeROM;
            readPages[38] = Pascal[6];
            writePages[38] = fakeROM;
            readPages[37] = Pascal[5];
            writePages[37] = fakeROM;
            readPages[36] = Pascal[4];
            writePages[36] = fakeROM;
            readPages[35] = Pascal[3];
            writePages[35] = fakeROM;
            readPages[34] = Pascal[2];
            writePages[34] = fakeROM;
            readPages[33] = Pascal[1];
            writePages[33] = fakeROM;
            readPages[32] = Pascal[0];
            writePages[32] = fakeROM;
        }
        if (hodn == 1) {
            readPages[47] = Pascal1[15];
            writePages[47] = fakeROM;
            readPages[46] = Pascal1[14];
            writePages[46] = fakeROM;
            readPages[45] = Pascal1[13];
            writePages[45] = fakeROM;
            readPages[44] = Pascal1[12];
            writePages[44] = fakeROM;
            readPages[43] = Pascal1[11];
            writePages[43] = fakeROM;
            readPages[42] = Pascal1[10];
            writePages[42] = fakeROM;
            readPages[41] = Pascal1[9];
            writePages[41] = fakeROM;
            readPages[40] = Pascal1[8];
            writePages[40] = fakeROM;
            readPages[39] = Pascal1[7];
            writePages[39] = fakeROM;
            readPages[38] = Pascal1[6];
            writePages[38] = fakeROM;
            readPages[37] = Pascal1[5];
            writePages[37] = fakeROM;
            readPages[36] = Pascal1[4];
            writePages[36] = fakeROM;
            readPages[35] = Pascal1[3];
            writePages[35] = fakeROM;
            readPages[34] = Pascal1[2];
            writePages[34] = fakeROM;
            readPages[33] = Pascal1[1];
            writePages[33] = fakeROM;
            readPages[32] = Pascal1[0];
            writePages[32] = fakeROM;
        }
        if (hodn == 2) {
            readPages[47] = Assembler[15];
            writePages[47] = fakeROM;
            readPages[46] = Assembler[14];
            writePages[46] = fakeROM;
            readPages[45] = Assembler[13];
            writePages[45] = fakeROM;
            readPages[44] = Assembler[12];
            writePages[44] = fakeROM;
            readPages[43] = Assembler[11];
            writePages[43] = fakeROM;
            readPages[42] = Assembler[10];
            writePages[42] = fakeROM;
            readPages[41] = Assembler[9];
            writePages[41] = fakeROM;
            readPages[40] = Assembler[8];
            writePages[40] = fakeROM;
            readPages[39] = Assembler[7];
            writePages[39] = fakeROM;
            readPages[38] = Assembler[6];
            writePages[38] = fakeROM;
            readPages[37] = Assembler[5];
            writePages[37] = fakeROM;
            readPages[36] = Assembler[4];
            writePages[36] = fakeROM;
            readPages[35] = Assembler[3];
            writePages[35] = fakeROM;
            readPages[34] = Assembler[2];
            writePages[34] = fakeROM;
            readPages[33] = Assembler[1];
            writePages[33] = fakeROM;
            readPages[32] = Assembler[0];
            writePages[32] = fakeROM;
        }
        if (hodn == 3) {
            readPages[32] = writePages[32] = Ram[32];
            readPages[33] = writePages[33] = Ram[33];
            readPages[34] = writePages[34] = Ram[34];
            readPages[35] = writePages[35] = Ram[35];
            readPages[36] = writePages[36] = Ram[36];
            readPages[37] = writePages[37] = Ram[37];
            readPages[38] = writePages[38] = Ram[38];
            readPages[39] = writePages[39] = Ram[39];
            readPages[40] = writePages[40] = Ram[40];
            readPages[41] = writePages[41] = Ram[41];
            readPages[42] = writePages[42] = Ram[42];
            readPages[43] = writePages[43] = Ram[43];
            readPages[44] = writePages[44] = Ram[44];
            readPages[45] = writePages[45] = Ram[45];
            readPages[46] = writePages[46] = Ram[46];
            readPages[47] = writePages[47] = Ram[47];
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
        if (!loadRomAsFile(romsDirectory + cf.getMonStdRom(), MonStd, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/Monitor-std.rom", MonStd, 0, PAGE_SIZE * 4);
        }
        if (!loadRomAsFile(romsDirectory + cf.getMonDisRom(), MonDis, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/Monitor-dis.rom", MonDis, 0, PAGE_SIZE * 4);
        }
        if (!loadRomAsFile(romsDirectory + cf.getMonKomRom(), MonKom, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/Monitor-cpm-Variel.rom", MonKom, 0, PAGE_SIZE * 4);
        }
        if (!loadRomAsFile(romsDirectory + cf.getMonFelRom(), MonFel, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/Monitor-cpm-030188.rom", MonFel, 0, PAGE_SIZE * 4);
        }
        if (!loadRomAsFile(romsDirectory + cf.getBasic6Rom(), Basic6, 0, PAGE_SIZE * 8)) {
            loadRomAsResource("/roms/Basic6.rom", Basic6, 0, PAGE_SIZE * 8);
        }        
        if (!loadRomAsFile(romsDirectory + cf.getBasicGRom(), BasicG, 0, PAGE_SIZE * 8)) {
            loadRomAsResource("/roms/BasicG.rom", BasicG, 0, PAGE_SIZE * 8);
        }        
        if (!loadRomAsFile(romsDirectory + cf.getBasic6Rom(), BasicG2, 0, PAGE_SIZE * 4)) {
            loadRomAsResource("/roms/BasicG2.rom", BasicG2, 0, PAGE_SIZE * 4);
        }
        if (!loadRomAsFile(romsDirectory + cf.getAssemblerRom(), Assembler, 0, PAGE_SIZE * 16)) {
            loadRomAsResource("/roms/Assembler.rom", Assembler, 0, PAGE_SIZE * 16);
        }
        if (!loadRomAsFile(romsDirectory + cf.getPascalRom(), Pascal, 0, PAGE_SIZE * 16)) {
            loadRomAsResource("/roms/Pascal.rom", Pascal, 0, PAGE_SIZE * 16);
        }
        if (!loadRomAsFile(romsDirectory + cf.getPascal1Rom(), Pascal1, 0, PAGE_SIZE * 16)) {
            loadRomAsResource("/roms/Pascal1.rom", Pascal1, 0, PAGE_SIZE * 16);
        }        
        loadRomAsResource("/roms/Disc2.rom", Disc2, 0, PAGE_SIZE * 2);
        
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