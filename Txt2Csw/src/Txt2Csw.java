/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author admin
 */

public class Txt2Csw {
    
    private static JFileChooser fc = new JFileChooser();
    private static File fi, fo;

    public static void main(String args[]) {
        int val;
        fc.setDialogTitle("Open TXT file");
        val = Txt2Csw.fc.showOpenDialog(null);
        if (val == JFileChooser.APPROVE_OPTION) {
            fi = fc.getSelectedFile();
            fc.setDialogTitle("Save CSW file");
            val = Txt2Csw.fc.showSaveDialog(null);
            if (val == JFileChooser.APPROVE_OPTION) {
                try {
                    String s = fc.getSelectedFile().getCanonicalPath();
                    if (!s.endsWith(".csw")) { s = s + ".csw"; }
                    fo = new File(s);
                    procFile();
                } catch (IOException ex) {
                    Logger.getLogger(Txt2Csw.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("Konec");
    }

    private static void procFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fi));            
        CswFile csw = CswFile.openCswFile(fo);
        
        csw.setRecord(true);
        makeTone(csw,2000);
            
        String s;
        while(br.ready()) {
            s = br.readLine();
            for(int n=0; n<s.length(); n++) {
                procByte(csw, (byte) s.charAt(n));
            }
            procByte(csw, (byte) 0x0d);
            makeTone(csw, 500);
        }
        csw.setRecord(false);
        csw.close();
        br.close();
    }
    
    private static void procByte(CswFile csw, int b) {
        b |= 0x80;
        b ^= 0xff;
        make1(csw);
        for(int i=0; i<8; i++) {
            if ((b & 0x01) == 0) make0(csw);
            else make1(csw);
            b = (b >>> 1);
        }
    }
    
    private static void makeTone(CswFile csw, int x) {
        for(int n=0; n<x; n++) {
            make0(csw);
        }
    }
  
    private static void make1(CswFile csw) {
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);

        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
    }
    
    private static void make0(CswFile csw) {
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);
        csw.writeSample(true);

        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);
        csw.writeSample(false);

    }
}
