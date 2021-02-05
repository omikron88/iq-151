/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author tmilata
 */
public class SDRom extends Thread implements Pio8255Notify {

    public static FileInputStream reader = null;
    public static FileOutputStream writer = null;
    private boolean bRunning;
    private boolean bStopped;
    private Pio8255 pioStapper;
    private int nBufferSize = 4096;
    private int FAT_MAX_PATH = 128;
    private Iq m;//parent
    int bit2status, bit5status;
    private JLabel lblLED;
    int savnum=0;
    private boolean bSaving=false;
    public boolean bSdromIn=false;
    public Interrupt itrInter=null;
    javax.swing.ImageIcon icoLedGreen= new javax.swing.ImageIcon(getClass().getResource("/icons/green.png"));
    javax.swing.ImageIcon icoLedGray= new javax.swing.ImageIcon(getClass().getResource("/icons/gray.png"));
    
    //implementuje preruseni Atmegy, ktere je v realu pouzito pro ukladani souboru z IQ na SD-ROM
    public class Interrupt extends Thread {

        private boolean bRunning;
        private boolean bFinished;
        private SDRom sdrom;//parent

        public Interrupt(SDRom inRom) {
            sdrom = inRom;
            bRunning = false;
            bFinished = false;
        }

        public boolean isRunning() {
            return bRunning;
        }

        public boolean isFinished() {
            return bFinished;
        }

        public void run() {
            bRunning = true;
            prijemsouboru();
            bRunning = false;
            bFinished = true;
            sdrom.m.bSav=false;
        }
    }

    public SDRom(Iq inM) {
        m = inM; //parent
        initSDRoot();
        pioStapper = new Pio8255(this);
        bRunning = false;
        bStopped = true;
    }
    
    public void newInterrupt(){
       itrInter = new Interrupt(this);
    }
    
     public void startInterrupt(){
       itrInter.start();
     }
   
    public void setLED(JLabel inLed) {
        lblLED = inLed;
    }

    //přepne stav LED v pravem dolnim rohu
    private void blinkLED() {
        if (lblLED != null) {
            if (lblLED.getIcon() != icoLedGray) {
                turnoffLED();
            } else {
                turnonLED();
            }
        }
    }
    //zhasne LED v pravem dolnim rohu
    private void turnoffLED() {
        if (lblLED != null) {
            lblLED.setIcon(icoLedGray);
        }
    }
    //rozsviti zelenou LED v pravem dolnim rohu
    private void turnonLED() {
        if (lblLED != null) {
            lblLED.setIcon(icoLedGreen);
        }
    }

    public Pio8255 getPio() {
        return pioStapper;
    }

    public boolean isSaving() {
        return bSaving;
    }
    
    public void setSaving(boolean inSave) {
        bSaving=inSave;
    }
    //ukonci beh Atmegy
    public void stopThread() {
        bRunning = false;//prepnu flag pro vypnuti smycky threadu
        if(itrInter!=null){
         itrInter.bRunning=false;//prepnu flag pro vypnuti smycky threadu preruseni
        }
        int nMax = 20;
        while ((!bStopped) && (nMax > 0)) { //cekani 20 msec na ukonceni smycky
            nMax--;
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
        
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch (IOException ex) {
                reader = null;
            }
        }
        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (IOException ex) {
                writer = null;
            }
        }
    }

    //vytvori adresar SDRoot + ulozi loader
    public static void initSDRoot() {
        File f = new File(utils.Config.getMyPath() + "SDRoot");
        if (!(f.exists() && f.isDirectory())) {
            //create directory SDRoot
            f.mkdirs();
        }
        File loaderFile = new File(utils.Config.getMyPath() + "SDRoot/__BLOADER.BAS");
        if (!loaderFile.exists()) {
            InputStream inLoader = SDRom.class.getResourceAsStream("/roms/__BLOADER.BAS");
            if (inLoader != null) {
                try {
                    byte[] buffer = new byte[inLoader.available()];
                    inLoader.read(buffer);
                    inLoader.close();
                    OutputStream outStream = new FileOutputStream(loaderFile);
                    outStream.write(buffer);
                } catch (IOException ex) {
                    System.out.println("Error during write __BLOADER.BAS to SDRoot directory: " + ex.getMessage());
                }
            }
        }

    }

    //hlavni obsluha, simulujici beh Atmegy v SD-ROM modulu
    public void run() {
        bRunning = true;
        bStopped = false;
        bSdromIn=false;
        while (bRunning) {

            File[] listOfFiles = new File(utils.Config.getMyPath() + "SDRoot").listFiles();
            //nejdrive zjistim nejvyssi index souboru save
            for (int i = 0; i < listOfFiles.length; i++) {
                if(listOfFiles[i].getName().toUpperCase().startsWith("SAVE")){
                    String strNum=listOfFiles[i].getName().substring(4, 7);
                    int nSavNum=Integer.parseInt(strNum);
                    if(nSavNum>savnum) savnum=nSavNum;	
                }
            }
            savnum++;

            try {
                Thread.sleep(1000); //pockam 1 sekundu na inicializaci IQ151
            } catch (InterruptedException ex) {
            }
            port_output();
            byte[] buffer = new byte[nBufferSize];
            
            //odeslu __BLOADER.BAS
            File loaderFile = new File(utils.Config.getMyPath() + "SDRoot/__BLOADER.BAS");
            if (loaderFile.exists()) {
                try {
                    reader = new FileInputStream(loaderFile);
                    int cnt = 0;

                    while ((cnt = reader.read(buffer, 0, nBufferSize - 1)) > 0) {
                        send2IQOfficial(buffer, cnt, 1);
                        if (!bRunning) {
                            break;
                        }
                    }
                    reader.close();
                } catch (Exception ex) {
                    System.out.println("Error during open __BLOADER.BAS from SDRoot directory: " + ex.getMessage());
                }
            }
            if (!bRunning) {
                bStopped = true;
                return;
            }

            //loader odeslan cekam na prikazy z IQ151
            int nBajt = 0;            
            port_input();
            bSdromIn=true;
            do {
                turnonLED();
                nBajt = readbyte();
                if (!bRunning) {
                    break;
                }
                switch (nBajt) {
                    case 130:
                        //vrat dir                        
                        poslidir();
                        nBajt = 0;
                        break;
                    case 131:
                        //posli soubor
                        poslisoubor();
                        break;
                    default:
                        nBajt = 0;
                }
            } while (nBajt == 0);

            bRunning = false;

        }
        turnoffLED();
        bStopped = true;
    }

    //nastavi port na vstup
    void port_input() {
       // pioStapper.CpuWrite(pioStapper.PP_CWR, 129);
        bit2status = 1; //prepinace pro prenos z IQ151 do modulu nestandardni cestou
        bit5status = 0; //pocita se zmena hodnoty bitu oproti predchozimu stavu
    }

    //nastavi port na vystup
    void port_output() {
        m.yield();
        pioStapper.CpuWrite(pioStapper.PP_CWR, 180);
        pioStapper.CpuWrite(pioStapper.PP_CWR, 9);
        pioStapper.CpuWrite(pioStapper.PP_CWR, 5);
    }

    void sendbyte(int c) {
        //vlozi bajt na portA
        pioStapper.PeripheralWriteByte(pioStapper.PP_PortA, c);
    }

    void sendstrobe(int v) {
        //nastav strobe na predanou hodnotu v
        pioStapper.PeripheralChangeBit(pioStapper.PP_PortC, 4, v == 1);
    }

    boolean isack() {
        //kontroluje, je-li nastaven ACK bit
        return (pioStapper.PeripheralReadBit(pioStapper.PP_PortC, 5));
    }

    //cte 1 bajt z IQ151 pomoci bit-bangu
    int readbyte() {
        blinkLED();        
        int nRetByte = 0;
        //poslu, ze jsem ready na cteni
        if (bit2status == 0) {
            pioStapper.PeripheralChangeBit(pioStapper.PP_PortC, 2, false);
            bit2status = 1;
        } else {
            pioStapper.PeripheralChangeBit(pioStapper.PP_PortC, 2, true);
            bit2status = 0;
        }
        //pockam, az mi protejsek rekne, ze nastavil data
        while (isack() == (bit5status == 1)) {
            if (!bRunning) {
                break;
            }
           m.yield();//predam rizeni na IQ, kvuli urychleni zmeny bitu      
        }
        
        if (bit5status == 0) {
            bit5status = 1;
        } else {
            bit5status = 0;
        }
        nRetByte = pioStapper.PeripheralReadByte(pioStapper.PP_PortA);        
        return nRetByte;

    }

    //standardni prenos z modulu do IQ151, ktery pouziva mode2 8255
    int send2IQOfficial(byte[] buffer, int cnt, int nDetectOn) {
        int nRet = 0;
        turnonLED();
        sendstrobe(1);
        for (int k = 0; k < cnt; k++) {
            blinkLED();
            sendbyte(buffer[k]);  //vlozim data
            sendstrobe(0);//vlozim do latchA
            sendstrobe(1);//nastavim intr
            //detekce konce nahravani binarnich dat - IQ151 standardne nenacita posledni 2 bajty
            if ((cnt - k < 13) && (k >= 8)) {
                if ((buffer[k - 7] == 0x3A) && (buffer[k - 6] == 0x30) && (buffer[k - 5] == 0x30) && (buffer[k] == 0x30)) {
                    if (k + 1 <= cnt) {
                        if (buffer[k + 1] == 0x31) {
                            //debug("koncim bin");
                            nRet = 1;
                            if (nDetectOn == 1) {
                                break;
                            }
                        }
                    }
                }
            }
//cekam na potvrzeni o nacteni do IQ
            while (isack()) {
                if (!bRunning) {
                    break;//pro pripad poazadvku na stop threadu
                }
                m.yield();//predam rizeni na IQ, pro urychleni cekani
            }
        }

        return nRet;
    }

    //posle obsah adresare do IQ151
    void poslidir() {
        byte[] pombuf = new byte[5];
        byte[] cur_dir = new byte[FAT_MAX_PATH];
        //nactu argument, tj. o jaky dir se jedna
        int nLen;
        nLen = readbyte();
       // System.out.println(String.format( "Len=%02X", nLen));
        for (int k = 0; k < nLen; k++) {
            cur_dir[k] = (byte) readbyte();
          //    System.out.println(String.format( "%02X", cur_dir[k]));
        }
        bSdromIn=false;
        cur_dir[nLen] = '\0';
        String strDir = new String(cur_dir, 0, nLen);
        //System.out.println("GetDir-"+strDir); 
 
        port_output();
        //String strDir2 = Paths.get(strDir).normalize().toString(); //JAVA 1.7
        String strDir2 = simplify(strDir); //JAVA 1.6
        File fDir = new File(utils.Config.getMyPath() + "SDRoot" + strDir);

        //nejdrive poslu dvojteckovy adresar umoznujici pohyb o uroven vyse, ale pouze do urovne SDRoot
        if ((!strDir2.contentEquals("/"))&&(!strDir2.contentEquals("\\"))) {
            pombuf[0] = '/';
            pombuf[1] = '.';
            pombuf[2] = '.';
            pombuf[3] = 0;
            send2IQ(pombuf, 4);
        }
        File[] listOfFiles = fDir.listFiles();
        //nejdrive poslu adresare
        for (int i = 0; i < listOfFiles.length; i++) {
            if (!bRunning) {
                break;
            }
            if (listOfFiles[i].isDirectory()) {
                pombuf[0] = '/';//adresar zacina lomitkem
                pombuf[1] = 0;
                send2IQ(pombuf, 1);
                send2IQ(listOfFiles[i].getName().getBytes(), listOfFiles[i].getName().length());
                pombuf[0] = 0; //konec texu je 0
                send2IQ(pombuf, 1);
            }
        }
        //ted poslu soubory
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (!listOfFiles[i].getName().equalsIgnoreCase("__BLOADER.BAS")) {
                    if (!bRunning) {
                        break;
                    }
                    send2IQ(listOfFiles[i].getName().getBytes(), listOfFiles[i].getName().length());
                    pombuf[0] = 0; //konec textu je 0
                    send2IQ(pombuf, 1);
                }
            }
        }

        pombuf[0] = 13;//konec celeho bloku je 13
        pombuf[1] = 0;
        send2IQ(pombuf, 1);
        port_input();
        bSdromIn=true;        
        turnoffLED();

    }

    //posle soubor do IQ151
    void poslisoubor() {
        byte[] file_mask = new byte[FAT_MAX_PATH];
        byte[] pom = new byte[10];
        //nactu argument, tj. o jaky soubor se jedna
        int nLen, cntlst = 0;
        nLen = readbyte();
        for (int k = 0; k < nLen; k++) {
            file_mask[k] = (byte) readbyte();
        }
        bSdromIn=false;
        file_mask[nLen] = '\0';

        String strFile = new String(file_mask, 0, nLen);

        port_output();

        byte[] buffer = new byte[nBufferSize];
        //odeslu vybrany soubor
        File fFile = new File(utils.Config.getMyPath() + "SDRoot" + strFile);
        if (fFile.exists()) {
            try {
                reader = new FileInputStream(fFile);
                int cnt = 0;
                int nTyp = 0, nKon = 0;
                while ((cnt = reader.read(buffer, 0, nBufferSize - 1)) > 0) {
                    int nSize = cnt;
                    buffer[cnt] = '\0';
                    //hledam escape - pro programy v BASICu
                    if (cnt >= 3) {
                        if (buffer[cnt - 3] == 0x1B) {
                            nSize -= 2;
                            nKon = 1;
                        }
                        if (buffer[cnt - 2] == 0x1B) {
                            nSize -= 1;
                            nKon = 1;
                        }
                        if (buffer[cnt - 1] == 0x1B) {
                            nKon = 1;
                        }
                    }
                    if (cnt < nBufferSize - 1) {
                        //posledni blok
                        nTyp = send2IQOfficial(buffer, nSize, 1);
                    } else {
                        nTyp = send2IQOfficial(buffer, nSize, 0);
                    }
                    cntlst = cnt;
                    if (nKon == 1) {
                        break;
                    }
                    if (!bRunning) {
                        break;
                    }

                }
                reader.close();
                if (nTyp == 0) {
                    //pokud neslo o binarni data
                    if (cntlst >= 3) {
                        if ((buffer[cntlst - 3] != 0x1B) && (buffer[cntlst - 2] != 0x1B) & (buffer[cntlst - 1] != 0x1B)) {
                            //odesli ESC pro pripad, ze ho basic soubor neobsahuje
                            buffer[0] = 0x1B;
                            send2IQOfficial(buffer, 1, 0);
                        }
                    }
                }

            } catch (Exception ex) {
                System.out.println("Error during open selected file: " + ex.getMessage());
            }
        }


        port_input();
        bSdromIn=true;
        turnoffLED();

    }
    
    
    void sendOUTAck(int v) {
        pioStapper.PeripheralChangeBit(pioStapper.PP_PortC, 2, v == 1);
    }

    boolean isOUTstrobe() {
        return (pioStapper.PeripheralReadBit(pioStapper.PP_PortC, 1));
    }


    public void prijemsouboru() {
        bSaving = true;
        byte[] pombuf = new byte[13];
        String strFileName=null;
        int nBajt = 0;
        byte[] pom = new byte[12];
        byte[] buffer = new byte[nBufferSize];
        int nFileOpened = 0;
        int bufpos = 0, nTyp = 0;
        //vynuluji buffer pro detekci konce
        Arrays.fill(pombuf, (byte) 0);
        port_output();
        sendOUTAck(1);
        while (true) {
            blinkLED();
            while (isOUTstrobe()) {
                m.yield();
            }; //cekam na zpravu, ze mam pripravena data
            sendOUTAck(0);
            //nactu bajt
            nBajt = pioStapper.PeripheralReadByte(pioStapper.PP_PortB);
            if (nBajt != 0) {//zajimaji me pouze nenulove bajty
                //sprintf(pom,"%c",nBajt);
                //debug(pom);
                if (nFileOpened == 0) {
                    strFileName = String.format("/save%03d.tmp", savnum);
                    savnum++;
                    try {
                        writer = new FileOutputStream(utils.Config.getMyPath() + "SDRoot" + strFileName);
                        nFileOpened = 1;
                    } catch (FileNotFoundException ex) {
                    }

                }
                for (int i = 12; i > 0; i--) {
                    pombuf[i] = pombuf[i - 1];
                }
                pombuf[0] = (byte) nBajt;
                buffer[bufpos] = (byte) nBajt;
                bufpos++;
                if ((writer != null) && (bufpos == nBufferSize)) {
                    try {
                        writer.write(buffer, 0, bufpos);
                    } catch (IOException ex) {
                    }
                    bufpos = 0;
                }
                //detekce konce ukladani
                //BASIC
                if ((pombuf[2] == 0x1B) && (pombuf[1] == 0x0D) && (pombuf[0] == 0x0A)) {
                    if (writer != null) {
                        try {
                            writer.write(buffer, 0, bufpos);
                            writer.write(pombuf, 0, 1);
                            writer.close();
                            writer = null;
                        } catch (IOException ex) {
                        }
                    }
                    //nasleduje 64 znaku 0
                    for (int i = 0; i < 64; i++) {
                        while (!isOUTstrobe()) {
                            m.yield();
                        };
                        sendOUTAck(1);
                        while (isOUTstrobe()) {
                            m.yield();
                        };
                        sendOUTAck(0);
                    }
                    nTyp = 0;
                    break;
                }
                //MONITOR
                if ((pombuf[12] == 0x3A) && (pombuf[11] == 0x30) && (pombuf[10] == 0x30) && (pombuf[5] == 0x30) && (pombuf[4] == 0x31) && (pombuf[1] == 0x0D) && (pombuf[0] == 0x0A)) {
                    if (writer != null) {
                        try {
                            writer.write(buffer, 0, bufpos);
                            writer.write(pombuf, 0, 1);
                            writer.close();
                            writer = null;
                        } catch (IOException ex) {
                        }
                        nTyp = 1;
                    }
                    break;
                }
            }

            //cekam na vzestupnou hranu
            while (!isOUTstrobe()) {
                m.yield();
            };
            //potvrdim, ze muzu cist dalsi bajt
            sendOUTAck(1);
        }
        //------------
        //rename podle typu
        if ((nFileOpened == 1)&&(strFileName!=null)) {
            StringBuilder strFileNameBuilder = new StringBuilder(strFileName);
            if (nTyp == 0) {
                strFileNameBuilder.setCharAt(9, 'b');
                strFileNameBuilder.setCharAt(10, 'a');
                strFileNameBuilder.setCharAt(11, 's');
            } else {
                strFileNameBuilder.setCharAt(9, 'h');
                strFileNameBuilder.setCharAt(10, 'e');
                strFileNameBuilder.setCharAt(11, 'x');
            }
            
            File oldfile =new File(utils.Config.getMyPath() + "SDRoot"+strFileName);
            File newfile =new File(utils.Config.getMyPath() + "SDRoot"+strFileNameBuilder);
            oldfile.renameTo(newfile);
        }
        turnoffLED();
        bSaving = false;
    }
    
    void send2IQ(byte[] buffer, long cnt) {
        sendstrobe(1);
        for (int k = 0; k < cnt; k++) {
            blinkLED();
            sendbyte(buffer[k]);  //vlozim data
            sendstrobe(0);//vlozim do latchA
            sendstrobe(1);//nastavim intr
            while (isack()) {
                m.yield();//predam rizeni na IQ, kvuli zrychleni prenosu
            }
        }
    }

    //odstrani double-dots z souborove cesty v Java 1.6
    //jedna se o ekvivalent Paths.normalize() v Jave 1.7
    static String simplify(String A) {
        Stack<String> st = new Stack<String>();
        String res = "";
        res += "/";
        int len_A = A.length();

        for (int i = 0; i < len_A; i++) {
            String dir = "";
            while (i < len_A && A.charAt(i) == '/') {
                i++;
            }
            while (i < len_A && A.charAt(i) != '/') {
                dir += A.charAt(i);
                i++;
            }

            if (dir.equals("..") == true) {
                if (!st.empty()) {
                    st.pop();
                }
            } else if (dir.equals(".") == true) {
                continue;
            } else if (dir.length() != 0) {
                st.push(dir);
            }
        }
        Stack<String> st1 = new Stack<String>();
        while (!st.empty()) {
            st1.push(st.pop());
        }
        while (!st1.empty()) {
            if (st1.size() != 1) {
                res += (st1.pop() + "/");
            } else {
                res += st1.pop();
            }
        }
        return res;
    }

    @Override
    public void OnCpuWriteA() {
    }

    @Override
    public void OnCpuWriteB() {
    }

    @Override
    public void OnCpuWriteC() {
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
    }

    @Override
    public void OnCpuReadB() {
    }

    @Override
    public void OnCpuReadC() {
    }

    @Override
    public void OnCpuReadCL() {
    }

    @Override
    public void OnCpuReadCH() {
    }
}
