/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Debugger.java
 *
 * Created on Oct 5, 2019, 8:51:34 PM
 */
package gui;

import disassemblers.I8080Dis;
import disassemblers.Z80Dis;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.JScrollPane;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import machine.Iq;



public class Debugger extends javax.swing.JFrame {

      public class StepBP{
        boolean bStatus;
        int nAdress;
        StepBP(){
          bStatus=false;
          nAdress=0; 
        }
    }

            
    private Iq m;
    public StepBP stpBP=new StepBP();
    javax.swing.ImageIcon icoRun= new javax.swing.ImageIcon(getClass().getResource("/icons/rundbg.png"));
    javax.swing.ImageIcon icoStop=new javax.swing.ImageIcon(getClass().getResource("/icons/stopdbg.png"));
    static int nBP1=-1;
    static int nBP2=-1;
    static int nBP3=-1;
    static int nBP4=-1;
    static int nBP5=-1;
    static int nBP6=-1;
    static int nDataShow=0;
    static boolean bBP1Checked=false;
    static boolean bBP2Checked=false;
    static boolean bBP3Checked=false;
    static boolean bBP4Checked=false;
    static boolean bBP5Checked=false;
    static boolean bBP6Checked=false;
    static boolean bBP1Updt=true;
    static boolean bShowCode=true;
    static boolean bZ80=false;
    
    static long nLastClick=0;
    static long nLastClickAsm=0;
    static int nStepPlusAsm=0;
    static Point pntPos=new Point(-1,-1);

    /** Creates new form Debugger */
    public Debugger(Iq inM) {
        initComponents();
        ((DefaultCaret)jTextAsmCode.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        jScrollPane1.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        ((DefaultCaret)jTextData.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        jScrollPane5.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        jRunButton.setIcon(icoRun);
        jTextData.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                int nIncStep = 8;
                int nDecStep = 8;
                if (!utils.Config.bShowCode) {
                    nDecStep = 1;
                    nIncStep = 1;
                    if(nStepPlusAsm!=0){
                      nIncStep=nStepPlusAsm;
                    }
                }
                if (e.getWheelRotation() < 0) {
                    //System.out.println("Up... " + e.getWheelRotation());
                    
                    nDataShow-=nDecStep;
                    if(nDataShow<0){
                        nDataShow=65536+nDataShow;
                    }
                    fillDataShow();
                } else {
                    //System.out.println("Down... " + e.getWheelRotation());
                    nDataShow+=nIncStep;
                    if(nDataShow>65535){
                        nDataShow=65536-nDataShow;
                    }
                    fillDataShow();
                }

            }
        });

        utils.Config.LoadConfig();
        if(utils.Config.bZ80){
            jButton1.setText("Z80");
        }
        bBP1Checked=utils.Config.bBP1;
        bBP2Checked=utils.Config.bBP2;
        bBP3Checked=utils.Config.bBP3;
        bBP4Checked=utils.Config.bBP4;
        bBP5Checked=utils.Config.bBP5;
        bBP6Checked=utils.Config.bBP6;
        nBP1=utils.Config.nBP1Address;
        nBP2=utils.Config.nBP2Address;
        nBP3=utils.Config.nBP3Address;
        nBP4=utils.Config.nBP4Address;
        nBP5=utils.Config.nBP5Address;
        nBP6=utils.Config.nBP6Address;
        nDataShow=utils.Config.nMemAddress;
        m = inM; 
        setBpoints();
    //     m.genDispTables();
         m.scr.repaint();
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {  
                pntPos=getLocation();
                m.startEmulation();
            }
        });
    }
    
    
    public void setBpoints(){
        if(bBP1Checked){
         m.cpu.setBreakpoint(nBP1, true);
        }
        if(bBP2Checked){
         m.cpu.setBreakpoint(nBP2, true);
        }
        if(bBP3Checked){
         m.cpu.setBreakpoint(nBP3, true);
        }
        if(bBP4Checked){
         m.cpu.setBreakpoint(nBP4, true);
        }
        if(bBP5Checked){
         m.cpu.setBreakpoint(nBP5, true);
        }
    }
    
     public void fillAsmCode(){
         if(utils.Config.bZ80) {
             fillAsmCodeZ80();
         }else{
             fillAsmCodeI8080();
         }
     }
    
    public void fillAsmCodeZ80(){
        //vyplni textove pole s assemblerem
        jTextAsmCode.setText("");
        Z80Dis disassembler=new Z80Dis();
        Z80Dis.Opcodes=new int[65536];
        int memPtr = m.cpu.getRegPC();
        for(int j=0;j<10;j++){
        int memPtrTmp=memPtr;
        for(int i=memPtr;i<memPtr+5;i++){
           Z80Dis.Opcodes[i]= (0xff) & (byte)m.mem.readByte(memPtrTmp);
           memPtrTmp++;
        }
        byte OpcodeLen = disassembler.OpcodeLen(memPtr);
        String instdata="";
        for(int i=memPtr;i<memPtr+OpcodeLen;i++){
          instdata+=String.format("%02X",(byte)Z80Dis.Opcodes[i]);  
        }
        for(int i=0;i<4-OpcodeLen;i++){
          instdata+="  ";  
        }
        String instrukce=String.format( "#%04X", memPtr)+" "+instdata+" "+disassembler.Disassemble(memPtr)+"\n";
        jTextAsmCode.append(instrukce); 
            if (j == 0) {
                Highlighter h = jTextAsmCode.getHighlighter();
                try {
                    h.addHighlight(0, instrukce.length()-1, DefaultHighlighter.DefaultPainter);
                } catch (Exception e1) {
                }
                stpBP.nAdress=memPtr+OpcodeLen;
                stpBP.bStatus=false;
            }
        memPtr+=OpcodeLen;
        }
    }
    
      public void fillAsmCodeI8080(){
        //vyplni textove pole s assemblerem
        jTextAsmCode.setText("");
        I8080Dis disassembler=new I8080Dis();
        I8080Dis.Opcodes=new int[65536];
        int memPtr = m.cpu.getRegPC();
        for(int j=0;j<10;j++){
        int memPtrTmp=memPtr;
        for(int i=memPtr;i<memPtr+5;i++){
           I8080Dis.Opcodes[i]= (0xff) & (byte)m.mem.readByte(memPtrTmp);
           memPtrTmp++;
        }

        disassembler.Disassemble(memPtr);
        byte OpcodeLen = I8080Dis.nInstrLen;
        
        String instdata="";
        for(int i=memPtr;i<memPtr+OpcodeLen;i++){
          instdata+=String.format("%02X",(byte)I8080Dis.Opcodes[i]);  
        }
        for(int i=0;i<4-OpcodeLen;i++){
          instdata+="  ";  
        }
        String instrukce=String.format( "#%04X", memPtr)+" "+instdata+" "+disassembler.Disassemble(memPtr)+"\n";
        jTextAsmCode.append(instrukce); 
            if (j == 0) {
                Highlighter h = jTextAsmCode.getHighlighter();
                try {
                    h.addHighlight(0, instrukce.length()-1, DefaultHighlighter.DefaultPainter);
                } catch (Exception e1) {
                }
                stpBP.nAdress=memPtr+OpcodeLen;
                stpBP.bStatus=false;
            }
        memPtr+=OpcodeLen;
        }
    }
    
    public void fillRegistry(){
     jTextRegistry.setText("");
     String strAF=String.format("AF:#%04X\n",m.cpu.getRegAF());  
     jTextRegistry.append(strAF);  
     String strBC=String.format("BC:#%04X\n",m.cpu.getRegBC());  
     jTextRegistry.append(strBC);   
     String strDE=String.format("DE:#%04X\n",m.cpu.getRegDE());  
     jTextRegistry.append(strDE); 
     String strHL=String.format("HL:#%04X\n",m.cpu.getRegHL());  
     jTextRegistry.append(strHL);
     String strPC=String.format("PC:#%04X\n",m.cpu.getRegPC());  
     jTextRegistry.append(strPC);
     String strSP=String.format("SP:#%04X\n",m.cpu.getRegSP());  
     jTextRegistry.append(strSP);

     DecimalFormat nf = new DecimalFormat("T:0000000000000000000");
     String strTStates=nf.format(m.cpu.clock.getTstates());  
     jLabelTStates.setText(strTStates);
    }
    
    public void fillFlags(){
        jTextFlags.setText("");
     int flags=m.cpu.getFlags();  

     if((flags & 0x80)!=0){
        jTextFlags.append("M\n");
     }else{
        jTextFlags.append("P\n"); 
     }
    if((flags & 0x40)!=0){
        jTextFlags.append("Z\n");
     }else{
        jTextFlags.append("NZ\n"); 
     }
     if((flags & 0x10)!=0){
        jTextFlags.append("AC\n");
     }else{
        jTextFlags.append("NA\n"); 
     }
     if((flags & 0x4)!=0){
        jTextFlags.append("PE\n");
     }else{
        jTextFlags.append("PO\n"); 
     }
     if((flags & 0x2)!=0){
        jTextFlags.append("N1\n");
     }else{
        jTextFlags.append("N0\n"); 
     }
     if((flags & 0x1)!=0){
        jTextFlags.append("C\n");
     }else{
        jTextFlags.append("NC\n"); 
     }
    }
    
    public void fillStack(){
       jTextStack.setText("");
       int nSPAdr=m.cpu.getRegSP();
       for(int i=nSPAdr;i<nSPAdr+10;i+=2){
          String strLine=String.format("#%04X #%04X\n",i,256*((0xff) & (byte)m.mem.readByte(i+1))+((0xff) & (byte)m.mem.readByte(i)));
          jTextStack.append(strLine);  
       }
      
       jTextStack.append("\n\n");
     // String strA0=String.format("port FE:#%02X\n",m.portA0);
     //jTextStack.append(strA0);
     //String strA1=String.format("port 0D:#%02X\n",m.portA1); 
     //jTextStack.append(strA1);
     //String strA2=String.format("port F7:#%02X\n",m.portA3); 
     //jTextStack.append(strA2);
    }
    
    public void fillBps(){
      jCheckBP1.setSelected(bBP1Checked);
      if(nBP1!=-1){
          jTextBP1.setText(String.format("%04X",nBP1));
      }
      
      jCheckBP2.setSelected(bBP2Checked);
      if(nBP2!=-1){
          jTextBP2.setText(String.format("%04X",nBP2));
      }
      
      jCheckBP3.setSelected(bBP3Checked);
      if(nBP3!=-1){
          jTextBP3.setText(String.format("%04X",nBP3));
      }
      
      jCheckBP4.setSelected(bBP4Checked);
      if(nBP4!=-1){
          jTextBP4.setText(String.format("%04X",nBP4));
      }
      
      jCheckBP5.setSelected(bBP5Checked);
      if(nBP5!=-1){
          jTextBP5.setText(String.format("%04X",nBP5));
      }
      
      jCheckBP6.setSelected(bBP6Checked);
      if(nBP6!=-1){
          jTextBP6.setText(String.format("%04X",nBP6));
      }
      
      utils.Config.bBP1=bBP1Checked;
      utils.Config.nBP1Address=nBP1;
      utils.Config.bBP2=bBP2Checked;
      utils.Config.nBP2Address=nBP2;
      utils.Config.bBP3=bBP3Checked;
      utils.Config.nBP3Address=nBP3;
      utils.Config.bBP4=bBP4Checked;
      utils.Config.nBP4Address=nBP4;
      utils.Config.bBP5=bBP5Checked;
      utils.Config.nBP5Address=nBP5;
      utils.Config.bBP6=bBP6Checked;
      utils.Config.nBP6Address=nBP6;
      utils.Config.SaveConfig();
    }
    
    public void fillDataShow() {
        if (utils.Config.bShowCode) {
            jRadioCode.setSelected(true);
            jRadioAssembler.setSelected(false);
        } else {
            jRadioCode.setSelected(false);
            jRadioAssembler.setSelected(true);
        }
        jTextAdr.setText(String.format("%04X", nDataShow));
        jTextData.setText("");
        int nMemAdr = Integer.valueOf(jTextAdr.getText(), 16);
        if (utils.Config.bShowCode) {
            nStepPlusAsm=0;
            //zobraz hexa vypis
            for (int i = 0; i < 7; i++) {
                String strAsci = "";
                String strOneLine = String.format("#%04X  ", nMemAdr);
                for (int j = 0; j < 8; j++) {
                    int nBajt = (0xff) & (byte) m.mem.readByte(nMemAdr);
                    if ((nBajt >= 32) && (nBajt <= 128)) {
                        strAsci += (char) (nBajt);
                    } else {
                        strAsci += ".";
                    }
                    strOneLine += String.format("%02X ", (0xff) & (byte) m.mem.readByte(nMemAdr));
                    nMemAdr++;
                    if (nMemAdr == 65536) {
                        nMemAdr = 0;
                    }
                }
                strOneLine += "   " + strAsci + "\n";
                jTextData.append(strOneLine);
            }
        } else {
            //zobraz disassembling 
           if(utils.Config.bZ80){
            //Z80
            Z80Dis disassembler = new Z80Dis();
            Z80Dis.Opcodes = new int[65600];
            int memPtr = nMemAdr;
            for (int j = 0; j < 7; j++) {
                int memPtrTmp = memPtr;
                for (int i = memPtr; i < memPtr + 5; i++) {
                    Z80Dis.Opcodes[i] = (0xff) & (byte) m.mem.readByte(memPtrTmp);
                    memPtrTmp++;
                    if (memPtrTmp == 65536) {
                        memPtrTmp = 0;
                    }
                }
                byte OpcodeLen = disassembler.OpcodeLen(memPtr);
                String instdata = "";
                for (int i = memPtr; i < memPtr + OpcodeLen; i++) {
                    instdata += String.format("%02X", (byte) Z80Dis.Opcodes[i]);
                }
                for (int i = 0; i < 4 - OpcodeLen; i++) {
                    instdata += "  ";
                }
                String instrukce = String.format("#%04X", memPtr) + " " + instdata + " " + disassembler.Disassemble(memPtr) + "\n";
                jTextData.append(instrukce);
                if (j == 0) {                
                    nStepPlusAsm=OpcodeLen;
                }
                memPtr += OpcodeLen;
                if(memPtr>65535){
                    memPtr=memPtr-65536;
                }
            }
            } else {
                //I8080
                I8080Dis disassembler = new I8080Dis();
                I8080Dis.Opcodes = new int[65600];
                int memPtr = nMemAdr;
                for (int j = 0; j < 7; j++) {
                    int memPtrTmp = memPtr;
                    for (int i = memPtr; i < memPtr + 5; i++) {
                        I8080Dis.Opcodes[i] = (0xff) & (byte) m.mem.readByte(memPtrTmp);
                        memPtrTmp++;
                        if (memPtrTmp == 65536) {
                            memPtrTmp = 0;
                        }
                    }
                    disassembler.Disassemble(memPtr);
                    byte OpcodeLen = I8080Dis.nInstrLen;
                    String instdata = "";
                    for (int i = memPtr; i < memPtr + OpcodeLen; i++) {
                        instdata += String.format("%02X", (byte) I8080Dis.Opcodes[i]);
                    }
                    for (int i = 0; i < 4 - OpcodeLen; i++) {
                        instdata += "  ";
                    }
                    String instrukce = String.format("#%04X", memPtr) + " " + instdata + " " + disassembler.Disassemble(memPtr) + "\n";
                    jTextData.append(instrukce);
                    if (j == 0) {
                        nStepPlusAsm = OpcodeLen;
                    }
                    memPtr += OpcodeLen;
                    if (memPtr > 65535) {
                        memPtr = memPtr - 65536;
                    }
                }
            }
        }
        utils.Config.nMemAddress = nDataShow;
        utils.Config.SaveConfig();
    }
    
    public void refreshDlg(){        
        fillAsmCode();
        fillRegistry();
        fillFlags();
        fillStack();
        fillBps();
        fillDataShow();  
    }

    public void showDialog() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if(pntPos.x<0){        
         setLocation((screen.width - getSize().width) / 2, (screen.height - getSize().height) / 2);
        }else{
         setLocation(pntPos);  
        }        
        if(stpBP.bStatus){
         //jedna se o step over
         jRunButton.setIcon(icoRun);
         jRunButton.setToolTipText("Run");
         m.cpu.setBreakpoint(stpBP.nAdress, false);
         stpBP.bStatus=false;
         setBpoints();
         jStepIntoButton.setEnabled(true);
         jStepButton.setEnabled(true);
         
        }
        refreshDlg();
        setVisible(true);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jRunButton = new javax.swing.JButton();
        jStepButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAsmCode = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextRegistry = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextFlags = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextStack = new javax.swing.JTextArea();
        jCheckBP1 = new javax.swing.JCheckBox();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextData = new javax.swing.JTextArea();
        jCheckBP2 = new javax.swing.JCheckBox();
        jCheckBP3 = new javax.swing.JCheckBox();
        jCheckBP4 = new javax.swing.JCheckBox();
        jCheckBP5 = new javax.swing.JCheckBox();
        jTextBP1 = new utils.JNumberTextField();
        jTextBP2 = new utils.JNumberTextField();
        jTextBP3 = new utils.JNumberTextField();
        jTextBP4 = new utils.JNumberTextField();
        jTextBP5 = new utils.JNumberTextField();
        jTextAdr = new utils.JNumberTextField();
        jLabelTStates = new javax.swing.JLabel();
        jStepIntoButton = new javax.swing.JButton();
        jRadioCode = new javax.swing.JRadioButton();
        jRadioAssembler = new javax.swing.JRadioButton();
        jCheckBP6 = new javax.swing.JCheckBox();
        jTextBP6 = new utils.JNumberTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Debugger");
        setResizable(false);

        jRunButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/rundbg.png"))); // NOI18N
        jRunButton.setToolTipText("Run");
        jRunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRunButtonActionPerformed(evt);
            }
        });

        jStepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/step.png"))); // NOI18N
        jStepButton.setToolTipText("Step Into");
        jStepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStepButtonActionPerformed(evt);
            }
        });

        jTextAsmCode.setEditable(false);
        jTextAsmCode.setColumns(20);
        jTextAsmCode.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        jTextAsmCode.setRows(6);
        jTextAsmCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextAsmCodeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTextAsmCode);

        jTextRegistry.setColumns(20);
        jTextRegistry.setEditable(false);
        jTextRegistry.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        jTextRegistry.setRows(5);
        jTextRegistry.setAutoscrolls(false);
        jScrollPane2.setViewportView(jTextRegistry);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextFlags.setColumns(20);
        jTextFlags.setEditable(false);
        jTextFlags.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        jTextFlags.setRows(5);
        jTextFlags.setAutoscrolls(false);
        jScrollPane3.setViewportView(jTextFlags);

        jTextStack.setColumns(20);
        jTextStack.setEditable(false);
        jTextStack.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        jTextStack.setRows(5);
        jScrollPane4.setViewportView(jTextStack);

        jCheckBP1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBP1ActionPerformed(evt);
            }
        });

        jTextData.setEditable(false);
        jTextData.setColumns(20);
        jTextData.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        jTextData.setRows(5);
        jScrollPane5.setViewportView(jTextData);

        jCheckBP2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBP2ActionPerformed(evt);
            }
        });

        jCheckBP3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBP3ActionPerformed(evt);
            }
        });

        jCheckBP4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBP4ActionPerformed(evt);
            }
        });

        jCheckBP5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBP5ActionPerformed(evt);
            }
        });

        jTextBP1.setText("0000");
        jTextBP1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBP1FocusLost(evt);
            }
        });

        jTextBP2.setText("0000");
        jTextBP2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBP2FocusLost(evt);
            }
        });

        jTextBP3.setText("0000");
        jTextBP3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBP3FocusLost(evt);
            }
        });

        jTextBP4.setText("0000");
        jTextBP4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBP4FocusLost(evt);
            }
        });

        jTextBP5.setText("0000");
        jTextBP5.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBP5FocusLost(evt);
            }
        });

        jTextAdr.setText("0000");
        jTextAdr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextAdrFocusLost(evt);
            }
        });

        jLabelTStates.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        jLabelTStates.setText("0");
        jLabelTStates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelTStatesMouseClicked(evt);
            }
        });

        jStepIntoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/stepover.png"))); // NOI18N
        jStepIntoButton.setToolTipText("Step Over");
        jStepIntoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStepIntoButtonActionPerformed(evt);
            }
        });

        jRadioCode.setText("Hex");
        jRadioCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioCodeActionPerformed(evt);
            }
        });

        jRadioAssembler.setText("Assembler");
        jRadioAssembler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioAssemblerActionPerformed(evt);
            }
        });

        jCheckBP6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBP6ActionPerformed(evt);
            }
        });

        jTextBP6.setText("0000");
        jTextBP6.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextBP6FocusLost(evt);
            }
        });

        jLabel1.setText("Mem Write Breakpoint");

        jButton1.setText("8080");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jZ80ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jRadioCode)
                                    .addComponent(jRadioAssembler)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBP1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextBP1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBP2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextBP2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBP3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextBP3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBP4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextBP4, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBP5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextBP5, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBP6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextBP6, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(jLabelTStates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jStepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jStepIntoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jRunButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(10, 10, 10)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jTextAdr, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRunButton, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                            .addComponent(jStepIntoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                            .addComponent(jStepButton, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                        .addGap(8, 8, 8))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBP1)
                    .addComponent(jCheckBP2)
                    .addComponent(jTextBP1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBP2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBP3)
                    .addComponent(jCheckBP4)
                    .addComponent(jTextBP3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBP5)
                    .addComponent(jTextBP4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextBP5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelTStates)))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBP6)
                            .addComponent(jTextBP6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextAdr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioAssembler)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jStepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStepButtonActionPerformed
       // m.cpu.setBreakpoint(nNextInstr, true);
        boolean bSaveSt=utils.Config.bBP6;
        jStepButton.setEnabled(false);
        if(m.cpu.bMemBP){
           //zastaveni na memory breakpointu         
           utils.Config.bBP6=false;
        }
        m.cpu.executeOne();
        //m.genDispTables();        
        m.scr.repaint();
        if(m.cpu.bMemBP){
           //zastaveni na memory breakpointu
           utils.Config.bBP6=bSaveSt;
        }
        refreshDlg();
        jStepButton.setEnabled(true);
    }//GEN-LAST:event_jStepButtonActionPerformed

    private void jRunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRunButtonActionPerformed
        if (jRunButton.getIcon() == icoStop) {
            m.stopEmulation();
            showDialog();
        } else {
            m.cpu.executeOne();
            m.startEmulation();
            pntPos = getLocation();
            this.dispose();
        }
    }//GEN-LAST:event_jRunButtonActionPerformed

    private void jCheckBP1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBP1ActionPerformed
      if(jCheckBP1.isSelected()){
       nBP1=Integer.valueOf(jTextBP1.getText(),16);
       //nastavit bp
       m.cpu.setBreakpoint(nBP1, true);
       bBP1Checked=true;
      }else{
        if(nBP1!=-1){
         //odstranit bp
          m.cpu.setBreakpoint(nBP1, false);
          bBP1Checked=false;
        }
      }
      fillBps();
    }//GEN-LAST:event_jCheckBP1ActionPerformed

    private void jCheckBP2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBP2ActionPerformed
       if(jCheckBP2.isSelected()){
       nBP2=Integer.valueOf(jTextBP2.getText(),16);
       //nastavit bp
       m.cpu.setBreakpoint(nBP2, true);
       bBP2Checked=true;
      }else{
        if(nBP2!=-1){
         //odstranit bp
          m.cpu.setBreakpoint(nBP2, false);
          bBP2Checked=false;
        }
      }
       fillBps();
    }//GEN-LAST:event_jCheckBP2ActionPerformed

    private void jCheckBP3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBP3ActionPerformed
     if(jCheckBP3.isSelected()){
       nBP3=Integer.valueOf(jTextBP3.getText(),16);
       //nastavit bp
       m.cpu.setBreakpoint(nBP3, true);
       bBP3Checked=true;
      }else{
        if(nBP3!=-1){
         //odstranit bp
          m.cpu.setBreakpoint(nBP3, false);
          bBP3Checked=false;
        }
      }
     fillBps();
    }//GEN-LAST:event_jCheckBP3ActionPerformed

    private void jCheckBP4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBP4ActionPerformed
       if(jCheckBP4.isSelected()){
       nBP4=Integer.valueOf(jTextBP4.getText(),16);
       //nastavit bp
       m.cpu.setBreakpoint(nBP4, true);
       bBP4Checked=true;
      }else{
        if(nBP4!=-1){
         //odstranit bp
          m.cpu.setBreakpoint(nBP4, false);
          bBP4Checked=false;
        }
      }
       fillBps();
    }//GEN-LAST:event_jCheckBP4ActionPerformed

    private void jCheckBP5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBP5ActionPerformed
      if(jCheckBP5.isSelected()){
       nBP5=Integer.valueOf(jTextBP5.getText(),16);
       //nastavit bp
       m.cpu.setBreakpoint(nBP5, true);
       bBP5Checked=true;
      }else{
        if(nBP5!=-1){
         //odstranit bp
          m.cpu.setBreakpoint(nBP5, false);
          bBP5Checked=false;
        }
      }
      fillBps();
    }//GEN-LAST:event_jCheckBP5ActionPerformed

    private void jTextAdrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextAdrFocusLost
      nDataShow=Integer.valueOf(jTextAdr.getText(),16);
       fillDataShow();
    }//GEN-LAST:event_jTextAdrFocusLost

    private void jLabelTStatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelTStatesMouseClicked
     Date date= new Date();
     long nNow=date.getTime();
     long nRozdil=(nNow-nLastClick);
     if((nRozdil>100)&&(nRozdil<500)){
       //double click
       m.cpu.clock.setTstates(0);
       fillRegistry();
       nLastClick=0;
     }else{
       nLastClick=nNow;  
     } 
    }//GEN-LAST:event_jLabelTStatesMouseClicked

    private void jTextBP1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBP1FocusLost
        int nNewAdr = Integer.valueOf(jTextBP1.getText(), 16);
        if (nNewAdr != nBP1) {
            if(bBP1Checked){
                m.cpu.setBreakpoint(nBP1, false);
            }
            nBP1 = nNewAdr;
            if(bBP1Checked){
                m.cpu.setBreakpoint(nBP1, true);
            }
        }
        fillBps();
    }//GEN-LAST:event_jTextBP1FocusLost

    private void jTextBP2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBP2FocusLost
        int nNewAdr = Integer.valueOf(jTextBP2.getText(), 16);
        if (nNewAdr != nBP2) {
            if(bBP2Checked){
                m.cpu.setBreakpoint(nBP2, false);
            }
            nBP2 = nNewAdr;
            if(bBP2Checked){
                m.cpu.setBreakpoint(nBP2, true);
            }
        }
        fillBps();
    }//GEN-LAST:event_jTextBP2FocusLost

    private void jTextBP3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBP3FocusLost
        int nNewAdr = Integer.valueOf(jTextBP3.getText(), 16);
        if (nNewAdr != nBP3) {
            if(bBP3Checked){
                m.cpu.setBreakpoint(nBP3, false);
            }
            nBP3 = nNewAdr;
            if(bBP3Checked){
                m.cpu.setBreakpoint(nBP3, true);
            }
        }
        fillBps();
    }//GEN-LAST:event_jTextBP3FocusLost

    private void jTextBP4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBP4FocusLost
        int nNewAdr = Integer.valueOf(jTextBP4.getText(), 16);
        if (nNewAdr != nBP4) {
            if(bBP4Checked){
                m.cpu.setBreakpoint(nBP4, false);
            }
            nBP4 = nNewAdr;
            if(bBP4Checked){
                m.cpu.setBreakpoint(nBP4, true);
            }
        }
        fillBps();
    }//GEN-LAST:event_jTextBP4FocusLost

    private void jTextBP5FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBP5FocusLost
        int nNewAdr = Integer.valueOf(jTextBP5.getText(), 16);
        if (nNewAdr != nBP5) {
            if(bBP5Checked){
                m.cpu.setBreakpoint(nBP5, false);
            }
            nBP5 = nNewAdr;
            if(bBP5Checked){
                m.cpu.setBreakpoint(nBP5, true);
            }
        }
        fillBps();
    }//GEN-LAST:event_jTextBP5FocusLost

    private void jTextAsmCodeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAsmCodeMouseClicked
      Date date= new Date();
     long nNow=date.getTime();
     long nRozdil=(nNow-nLastClickAsm);
     if((nRozdil>100)&&(nRozdil<500)){
       //double click
       if (jTextAsmCode.getSelectedText() != null) {
        String strSelected= jTextAsmCode.getSelectedText();
        if(strSelected.startsWith("#")){
          strSelected=strSelected.substring(1);  
        }
        int nBpAdress=-1;
        try{
            nBpAdress=Integer.valueOf(strSelected,16);
        }catch(java.lang.NumberFormatException e){
            
        }
         if((nBpAdress>=0)&&(nBpAdress<=65535)){
                 boolean bOK = false;
                 if (!bBP1Checked) {
                     nBP1 = nBpAdress;
                     bBP1Checked=true;
                     m.cpu.setBreakpoint(nBP1, true);
                     fillBps();
                     bOK=true;
                 }
                 if ((!bBP2Checked)&&(!bOK)) {
                     nBP2 = nBpAdress;
                     bBP2Checked=true;
                     m.cpu.setBreakpoint(nBP2, true);
                     fillBps();
                     bOK=true;
                 }
                 if ((!bBP3Checked)&&(!bOK)) {
                     nBP3 = nBpAdress;
                     bBP3Checked=true;
                     m.cpu.setBreakpoint(nBP3, true);
                     fillBps();
                     bOK=true;
                 }
                 if ((!bBP4Checked)&&(!bOK)) {
                     nBP4 = nBpAdress;
                     bBP4Checked=true;
                     m.cpu.setBreakpoint(nBP4, true);
                     fillBps();
                     bOK=true;
                 }
                 if ((!bBP5Checked)&&(!bOK)) {
                     nBP5 = nBpAdress;
                     bBP5Checked=true;
                     m.cpu.setBreakpoint(nBP5, true);
                     fillBps();
                     bOK=true;
                 }
                 if(!bOK){
                     //vsechny BP jsou obsazeny, vyberu hned prvni
                     m.cpu.setBreakpoint(nBP1, false);
                     nBP1 = nBpAdress;
                     m.cpu.setBreakpoint(nBP1, true);
                     fillBps();
                 }
             }
         }
       nLastClickAsm=0;
     }else{
       nLastClickAsm=nNow;  
     } 
    }//GEN-LAST:event_jTextAsmCodeMouseClicked

    private void jStepIntoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStepIntoButtonActionPerformed
        m.cpu.setBreakpoint(m.cpu.getRegPC(),false);//vypnu pripadny breakpoint na aktualni adrese, jinak bych se nikam nepohnul
        m.cpu.setBreakpoint(stpBP.nAdress, true);
        stpBP.bStatus=true;
        //JOptionPane.showMessageDialog(null, String.format("%04X",nNextInstr));
        if(m.cpu.bMemBP){
           //zastaveni na memory breakpointu
           boolean bSaveSt=utils.Config.bBP6;
           utils.Config.bBP6=false;
           m.cpu.executeOne();
           utils.Config.bBP6=true;
           utils.Config.bBP6=bSaveSt;
        }
        m.startEmulation();
        pntPos=getLocation();
        jStepIntoButton.setEnabled(false);
        jStepButton.setEnabled(false);
        jRunButton.setIcon(icoStop);
        jRunButton.setToolTipText("Stop");
        //this.dispose();
    }//GEN-LAST:event_jStepIntoButtonActionPerformed

    private void jRadioCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioCodeActionPerformed
       utils.Config.bShowCode=true;
       utils.Config.SaveConfig();
       refreshDlg();
    }//GEN-LAST:event_jRadioCodeActionPerformed

    private void jRadioAssemblerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioAssemblerActionPerformed
       utils.Config.bShowCode=false;
       utils.Config.SaveConfig();
       refreshDlg();
    }//GEN-LAST:event_jRadioAssemblerActionPerformed

    private void jCheckBP6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBP6ActionPerformed
       if(jCheckBP6.isSelected()){
       bBP6Checked=true;
      }else{
          bBP6Checked=false;
        
      }
      fillBps();
    }//GEN-LAST:event_jCheckBP6ActionPerformed

    private void jTextBP6FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextBP6FocusLost
        int nNewAdr = Integer.valueOf(jTextBP6.getText(), 16);
        if (nNewAdr != nBP6) {
            nBP6 = nNewAdr;
        }
        fillBps();
    }//GEN-LAST:event_jTextBP6FocusLost

    private void jZ80ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jZ80ActionPerformed
        if (utils.Config.bZ80) {
            utils.Config.bZ80 = false;
            jButton1.setText("8080");
        } else {
            utils.Config.bZ80 = true;
            jButton1.setText("Z80");
        }
        utils.Config.SaveConfig();
        refreshDlg();
    }//GEN-LAST:event_jZ80ActionPerformed


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBP1;
    private javax.swing.JCheckBox jCheckBP2;
    private javax.swing.JCheckBox jCheckBP3;
    private javax.swing.JCheckBox jCheckBP4;
    private javax.swing.JCheckBox jCheckBP5;
    private javax.swing.JCheckBox jCheckBP6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelTStates;
    private javax.swing.JRadioButton jRadioAssembler;
    private javax.swing.JRadioButton jRadioCode;
    private javax.swing.JButton jRunButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton jStepButton;
    private javax.swing.JButton jStepIntoButton;
    private utils.JNumberTextField jTextAdr;
    private javax.swing.JTextArea jTextAsmCode;
    private utils.JNumberTextField jTextBP1;
    private utils.JNumberTextField jTextBP2;
    private utils.JNumberTextField jTextBP3;
    private utils.JNumberTextField jTextBP4;
    private utils.JNumberTextField jTextBP5;
    private utils.JNumberTextField jTextBP6;
    private javax.swing.JTextArea jTextData;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextArea jTextFlags;
    private javax.swing.JTextArea jTextRegistry;
    private javax.swing.JTextArea jTextStack;
    // End of variables declaration//GEN-END:variables
}
