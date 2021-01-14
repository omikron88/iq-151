/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import gui.JIQ151;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


/**
 *
 * @author Administrator
 */
public class Config {
    
    //ulozene udaje pro nahravani binarniho souboru
    public static String strBinFilePath="";
    public static int nBeginBinAddress=0;
    public static boolean bRunBin=false;
    public static int nRunBinAddress=0;
    public static boolean bAllRam=true;
    public static boolean bHeaderOn=true;
    //ulozene udaje pro debugger
    public static boolean bBP1=false;
    public static int nBP1Address=0;
    public static boolean bBP2=false;
    public static int nBP2Address=0;
    public static boolean bBP3=false;
    public static int nBP3Address=0;
    public static boolean bBP4=false;
    public static int nBP4Address=0;
    public static boolean bBP5=false;
    public static int nBP5Address=0;
    public static boolean bBP6=false;
    public static int nBP6Address=0;
    public static int nMemAddress=0;
    public static boolean bShowCode=false;
    //ulozene udaje pro ukladani do binarniho souboru
    public static String strSaveBinFilePath="";
    public static int nSaveFromAddress=0;
    public static int nSaveToAddress=0;
    
    //ukladani settings
    public static int mainmodule=2;
    public static boolean grafik=true;
    public static boolean staper=false;
    public static int video64=0;
    public static boolean mem64=false;
    
    public static String getMyPath() {
        String retVal = "";
        retVal = JIQ151.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (retVal.contains("/")) {
            int pos = retVal.lastIndexOf("/");
            retVal = retVal.substring(0, pos + 1);
        }
        return retVal;
    }

    public static void SaveConfig() {
     Properties prop = new Properties();
     prop.setProperty("BINFILEPATH", strBinFilePath);
     prop.setProperty("BEGINBINADDRESS", String.valueOf(nBeginBinAddress));
     prop.setProperty("BRUNBIN", String.valueOf(bRunBin));
     prop.setProperty("RUNBINADDRESS", String.valueOf(nRunBinAddress));
     prop.setProperty("BALLRAM", String.valueOf(bAllRam));
     prop.setProperty("BHEADER", String.valueOf(bHeaderOn));
     prop.setProperty("BP1CHCK", String.valueOf(bBP1));
     prop.setProperty("BP1ADDRESS", String.valueOf(nBP1Address));
     prop.setProperty("BP2CHCK", String.valueOf(bBP2));
     prop.setProperty("BP2ADDRESS", String.valueOf(nBP2Address));
     prop.setProperty("BP3CHCK", String.valueOf(bBP3));
     prop.setProperty("BP3ADDRESS", String.valueOf(nBP3Address));
     prop.setProperty("BP4CHCK", String.valueOf(bBP4));
     prop.setProperty("BP4ADDRESS", String.valueOf(nBP4Address));
     prop.setProperty("BP5CHCK", String.valueOf(bBP5));
     prop.setProperty("BP5ADDRESS", String.valueOf(nBP5Address));   
     prop.setProperty("BP6CHCK", String.valueOf(bBP6));
     prop.setProperty("BP6ADDRESS", String.valueOf(nBP6Address));  
     prop.setProperty("MEMADDRESS", String.valueOf(nMemAddress)); 
     prop.setProperty("BSHOWCODE", String.valueOf(bShowCode)); 
     
     prop.setProperty("BINSAVEFILEPATH", String.valueOf(strSaveBinFilePath));
     prop.setProperty("BINSAVEADDRESSFROM", String.valueOf(nSaveFromAddress)); 
     prop.setProperty("BINSAVEADDRESSTO", String.valueOf(nSaveToAddress)); 
     
     prop.setProperty("MAINMODULE", String.valueOf(mainmodule));
     prop.setProperty("GRAFIK", String.valueOf(grafik));
     prop.setProperty("STAPER", String.valueOf(staper));
     prop.setProperty("VIDEO64", String.valueOf(video64));   
     prop.setProperty("MEM64", String.valueOf(mem64)); 

      String fileName = getMyPath() + "JIQ151.config";
        OutputStream os;
        try {
            os = new FileOutputStream(fileName);
            try {
                prop.store(os,"JIQ151 config file");
            } catch (IOException ex) {
                System.out.println("Nelze ulozit " + fileName);
            }
            os.close();
        } catch (Exception ex) {
            
        }
    }

    public static void LoadConfig() {
        Properties prop = new Properties();
        String fileName = getMyPath() + "JIQ151.config";
        InputStream is;
        try {
            is = new FileInputStream(fileName);
            try {
                prop.load(is);
            } catch (IOException ex) {
                System.out.println("Nelze rozparsovat " + fileName);
            }
        } catch (FileNotFoundException ex) {                      
                SaveConfig(); 
                return;
        }

        strBinFilePath = prop.getProperty("BINFILEPATH");
        nBeginBinAddress=Integer.parseInt(prop.getProperty("BEGINBINADDRESS"));
        bRunBin=Boolean.parseBoolean(prop.getProperty("BRUNBIN"));
        nRunBinAddress=Integer.parseInt(prop.getProperty("RUNBINADDRESS")); 
        bAllRam=Boolean.parseBoolean(prop.getProperty("BALLRAM"));
        bHeaderOn=Boolean.parseBoolean(prop.getProperty("BHEADER"));        
        bBP1=Boolean.parseBoolean(prop.getProperty("BP1CHCK"));
        nBP1Address=Integer.parseInt(prop.getProperty("BP1ADDRESS"));
        bBP2=Boolean.parseBoolean(prop.getProperty("BP2CHCK"));
        nBP2Address=Integer.parseInt(prop.getProperty("BP2ADDRESS"));
        bBP3=Boolean.parseBoolean(prop.getProperty("BP3CHCK"));
        nBP3Address=Integer.parseInt(prop.getProperty("BP3ADDRESS"));
        bBP4=Boolean.parseBoolean(prop.getProperty("BP4CHCK"));
        nBP4Address=Integer.parseInt(prop.getProperty("BP4ADDRESS"));
        bBP5=Boolean.parseBoolean(prop.getProperty("BP5CHCK"));
        nBP5Address=Integer.parseInt(prop.getProperty("BP5ADDRESS"));
        bBP6=Boolean.parseBoolean(prop.getProperty("BP6CHCK"));
        nBP6Address=Integer.parseInt(prop.getProperty("BP6ADDRESS"));
        nMemAddress=Integer.parseInt(prop.getProperty("MEMADDRESS"));
        bShowCode=Boolean.parseBoolean(prop.getProperty("BSHOWCODE"));
        strSaveBinFilePath = prop.getProperty("BINSAVEFILEPATH");
        nSaveFromAddress=Integer.parseInt(prop.getProperty("BINSAVEADDRESSFROM"));
        nSaveToAddress=Integer.parseInt(prop.getProperty("BINSAVEADDRESSTO"));
        
        mainmodule=Integer.parseInt(prop.getProperty("MAINMODULE"));
        grafik=Boolean.parseBoolean(prop.getProperty("GRAFIK"));
        staper=Boolean.parseBoolean(prop.getProperty("STAPER"));
        video64=Integer.parseInt(prop.getProperty("VIDEO64"));
        mem64=Boolean.parseBoolean(prop.getProperty("MEM64"));
  
    }
}
