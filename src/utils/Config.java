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
    public static String strBinFilePath = "";
    public static int nBeginBinAddress = 0;
    public static boolean bRunBin = false;
    public static int nRunBinAddress = 0;
    public static boolean bAllRam = true;
    public static boolean bHeaderOn = true;
    //ulozene udaje pro debugger
    public static boolean bBP1 = false;
    public static int nBP1Address = 0;
    public static boolean bBP2 = false;
    public static int nBP2Address = 0;
    public static boolean bBP3 = false;
    public static int nBP3Address = 0;
    public static boolean bBP4 = false;
    public static int nBP4Address = 0;
    public static boolean bBP5 = false;
    public static int nBP5Address = 0;
    public static boolean bBP6 = false;
    public static int nBP6Address = 0;
    public static int nMemAddress = 0;
    public static boolean bShowCode = false;
    public static boolean bZ80 = false;
    //ulozene udaje pro ukladani do binarniho souboru
    public static String strSaveBinFilePath = "";
    public static int nSaveFromAddress = 0;
    public static int nSaveToAddress = 0;
    public static String strShotFilePath = "";
    public static boolean bDisc2 = false;
    public static String strFlop1FilePath = "";
    public static String strFlop2FilePath = "";
    public static boolean bFlop1RW = false;
    public static boolean bFlop2RW = false;
    public static boolean bFlop1Inserted = false;
    public static boolean bFlop2Inserted = false;
    //ukladani settings
    public static int mainmodule = 2;
    public static int monitor = 10;
    public static boolean grafik = true;
    public static boolean sdrom = false;
    public static boolean sdromautorun = false;
    public static int video64 = 0;
    public static boolean mem64 = false;
    public static boolean audio = true;
    public static boolean felautorun = false;
    public static boolean amosautorun = false;
    
    public static boolean smartkeyboard = false;

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
        prop.setProperty("BZ80", String.valueOf(bZ80));

        prop.setProperty("BINSAVEFILEPATH", String.valueOf(strSaveBinFilePath));
        prop.setProperty("BINSAVEADDRESSFROM", String.valueOf(nSaveFromAddress));
        prop.setProperty("BINSAVEADDRESSTO", String.valueOf(nSaveToAddress));
        prop.setProperty("SHOTFILEPATH", String.valueOf(strShotFilePath));
        prop.setProperty("FLOP1FILEPATH", String.valueOf(strFlop1FilePath));
        prop.setProperty("FLOP1RW", String.valueOf(bFlop1RW));
        prop.setProperty("FLOP1INSERTED", String.valueOf(bFlop1Inserted));
        prop.setProperty("FLOP2FILEPATH", String.valueOf(strFlop2FilePath));
        prop.setProperty("FLOP2RW", String.valueOf(bFlop2RW));
        prop.setProperty("FLOP2INSERTED", String.valueOf(bFlop2Inserted));

        prop.setProperty("MAINMODULE", String.valueOf(mainmodule));
        prop.setProperty("GRAFIK", String.valueOf(grafik));
        prop.setProperty("SDROM", String.valueOf(sdrom));
        prop.setProperty("SDROMAUTO", String.valueOf(sdromautorun));
        prop.setProperty("FELAUTO", String.valueOf(felautorun));
        prop.setProperty("AMOSAUTO", String.valueOf(amosautorun));
        prop.setProperty("AUDIO", String.valueOf(audio));
        prop.setProperty("VIDEO64", String.valueOf(video64));
        prop.setProperty("MEM64", String.valueOf(mem64));
        prop.setProperty("MONITOR", String.valueOf(monitor));
        prop.setProperty("BDISC2", String.valueOf(bDisc2));
        
        prop.setProperty("SMARTKBD", String.valueOf(smartkeyboard));
        

        String fileName = getMyPath() + "JIQ151.config";
        OutputStream os;
        try {
            os = new FileOutputStream(fileName);
            try {
                prop.store(os, "JIQ151 config file");
            } catch (IOException ex) {
                System.out.println("Nelze ulozit " + fileName);
            }
            os.close();
        } catch (Exception ex) {

        }
    }

    private static int parseIntSafe(String strInt, int nDefault) {
        int nRet = nDefault;
        try {
            nRet = Integer.parseInt(strInt);
        } catch (Exception e) {
            nRet = nDefault;
        }
        return nRet;
    }

    public static String nullToEmpty(String strIn) {
        if (strIn == null) {
            strIn = "";
        }
        return strIn;
    }

    private static boolean parseBooleanSafe(String strBoolean, boolean bDefault) {
        boolean bRet = bDefault;
        try {
            bRet = Boolean.parseBoolean(strBoolean);
        } catch (Exception e) {
            bRet = bDefault;
        }
        return bRet;
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
        nBeginBinAddress = parseIntSafe(prop.getProperty("BEGINBINADDRESS"), 0);
        bRunBin = parseBooleanSafe(prop.getProperty("BRUNBIN"), false);
        nRunBinAddress = parseIntSafe(prop.getProperty("RUNBINADDRESS"), 0);
        bAllRam = parseBooleanSafe(prop.getProperty("BALLRAM"), true);
        bHeaderOn = parseBooleanSafe(prop.getProperty("BHEADER"), true);
        bBP1 = parseBooleanSafe(prop.getProperty("BP1CHCK"), false);
        nBP1Address = parseIntSafe(prop.getProperty("BP1ADDRESS"), 0);
        bBP2 = parseBooleanSafe(prop.getProperty("BP2CHCK"), false);
        nBP2Address = parseIntSafe(prop.getProperty("BP2ADDRESS"), 0);
        bBP3 = parseBooleanSafe(prop.getProperty("BP3CHCK"), false);
        nBP3Address = parseIntSafe(prop.getProperty("BP3ADDRESS"), 0);
        bBP4 = parseBooleanSafe(prop.getProperty("BP4CHCK"), false);
        nBP4Address = parseIntSafe(prop.getProperty("BP4ADDRESS"), 0);
        bBP5 = parseBooleanSafe(prop.getProperty("BP5CHCK"), false);
        nBP5Address = parseIntSafe(prop.getProperty("BP5ADDRESS"), 0);
        bBP6 = parseBooleanSafe(prop.getProperty("BP6CHCK"), false);
        nBP6Address = parseIntSafe(prop.getProperty("BP6ADDRESS"), 0);
        nMemAddress = parseIntSafe(prop.getProperty("MEMADDRESS"), 0);
        bShowCode = parseBooleanSafe(prop.getProperty("BSHOWCODE"), false);
        bZ80 = parseBooleanSafe(prop.getProperty("BZ80"), false);
        strSaveBinFilePath = prop.getProperty("BINSAVEFILEPATH");
        nSaveFromAddress = parseIntSafe(prop.getProperty("BINSAVEADDRESSFROM"), 0);
        nSaveToAddress = parseIntSafe(prop.getProperty("BINSAVEADDRESSTO"), 0);

        mainmodule = parseIntSafe(prop.getProperty("MAINMODULE"), 2);
        monitor = parseIntSafe(prop.getProperty("MONITOR"), 10);
        grafik = parseBooleanSafe(prop.getProperty("GRAFIK"), true);
        sdrom = parseBooleanSafe(prop.getProperty("SDROM"), false);
        sdromautorun = parseBooleanSafe(prop.getProperty("SDROMAUTO"), false);
        felautorun = parseBooleanSafe(prop.getProperty("FELAUTO"), false);
        amosautorun = parseBooleanSafe(prop.getProperty("AMOSAUTO"), false);        
        audio = parseBooleanSafe(prop.getProperty("AUDIO"), true);
        video64 = parseIntSafe(prop.getProperty("VIDEO64"), 0);
        mem64 = parseBooleanSafe(prop.getProperty("MEM64"), false);
        strShotFilePath = nullToEmpty(prop.getProperty("SHOTFILEPATH"));
        bDisc2 = parseBooleanSafe(prop.getProperty("BDISC2"), false);
        strFlop1FilePath = nullToEmpty(prop.getProperty("FLOP1FILEPATH"));
        strFlop2FilePath = nullToEmpty(prop.getProperty("FLOP2FILEPATH"));
        bFlop1RW=parseBooleanSafe(prop.getProperty("FLOP1RW"), false);
        bFlop2RW=parseBooleanSafe(prop.getProperty("FLOP2RW"), false);
        bFlop1Inserted=parseBooleanSafe(prop.getProperty("FLOP1INSERTED"), false);
        bFlop2Inserted=parseBooleanSafe(prop.getProperty("FLOP2INSERTED"), false);
        smartkeyboard=parseBooleanSafe(prop.getProperty("SMARTKBD"), false);        
    }
}
