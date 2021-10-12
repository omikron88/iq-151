/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import machine.Config;
import machine.Iq;
import machine.Keyboard;
import machine.SDRom;

/**
 *
 * @author Administrator
 */
public class JIQ151 extends javax.swing.JFrame {
    
    private final String loadExt[] = {".wav",".csw"};
    private final String saveExt[] = {".csw"};
    
    private final ExtendedFileFilter loadFlt = new ExtendedFileFilter
            ("WAV or CSW files",loadExt);
    private final ExtendedFileFilter saveFlt = new ExtendedFileFilter
            ("CSW files",saveExt);    
    
    
    public JPanel ledPanel=null;
    public JLabel lblLed=null;
    private Iq m;
    private JIQScreen scr;
    private Debugger deb;
    private BinOpen bopn;
    private BinSave bsav;
    javax.swing.ImageIcon icoRun= new javax.swing.ImageIcon(getClass().getResource("/icons/run.png"));
    javax.swing.ImageIcon icoPause= new javax.swing.ImageIcon(getClass().getResource("/icons/pause.png"));
    javax.swing.ImageIcon icoFloppyRO= new javax.swing.ImageIcon(getClass().getResource("/icons/floppy.png"));
    javax.swing.ImageIcon icoFloppyRW= new javax.swing.ImageIcon(getClass().getResource("/icons/floppyrw.png"));

    
    /**
     * Creates new form JIQ151
     */
    public JIQ151() {
        initComponents();
        setIconImage((new ImageIcon(getClass().getResource("/icons/iq.png")).getImage()));
        //presun polozky menu About doprava
        jMenuBar1.remove(jAbout);
        jMenuBar1.add(Box.createHorizontalGlue());
        jMenuBar1.add(jAbout);        
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        initEmulator();

        
        lblLed = new JLabel("");
        lblLed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gray.png")));
        m.setSDRomLED(lblLed);         
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(lblLed);
        statusPanel.add(Box.createRigidArea(new Dimension(4,0)));
        
        jCheckBoxSpeed05.setSelected(false);
        jCheckBoxspeed1.setSelected(true);
        jCheckBoxSpeed2.setSelected(false);
        jCheckBoxSpeed3.setSelected(false);
        jCheckBoxSpeed4.setSelected(false);
        jCheckBoxSpeed5.setSelected(false);
        m.setSpeed(2);
        
        if(utils.Config.sdrom){
            addLEDbar();
        }else{
            removeLEDbar();
        } 
        floppyUpdate();
        if(utils.Config.bDisc2){
            floppyON();
        }else{
            floppyOFF();
        }        
    }

    private void addLEDbar() {
        lblLed.setVisible(true);
    }
    
    private void removeLEDbar(){
       lblLed.setVisible(false);
    }
    
    private void floppyON() {
        jFloppyButton1.setVisible(true);
        jFloppy1.setVisible(true);
        jFloppyButton2.setVisible(true);
        jFloppy2.setVisible(true);
    }

    private void floppyOFF() {
        jFloppyButton1.setVisible(false);
        jFloppy1.setVisible(false);
        jFloppyButton2.setVisible(false);
        jFloppy2.setVisible(false);
    }
    

    private void floppyUpdate() {
         jFloppyButton1.setIcon(icoFloppyRO);
         jFloppyButton2.setIcon(icoFloppyRO);
         Arrays.fill(m.floppyA.dimage,(byte)0);
         Arrays.fill(m.floppyB.dimage,(byte)0);
         jFloppy1.setText("Empty");
         jFloppy1.setToolTipText("Empty");
         jFloppy2.setText("Empty");
         jFloppy2.setToolTipText("Empty");         
        if (utils.Config.strFlop1FilePath.isEmpty()) {
            utils.Config.bFlop1Inserted=false;
        } else { 
           if(utils.Config.bFlop1Inserted){
            try {
                InputStream is;
                is = new DataInputStream(new FileInputStream(utils.Config.strFlop1FilePath));
                    try {
                        if (is.available() != 256256) {                              
                            JOptionPane.showMessageDialog(null, "Invalid size of floppy image "+new File(utils.Config.strFlop1FilePath).getName()+"\nThe allowed size is 256256 bytes", "Error", JOptionPane.ERROR_MESSAGE);
                            jFloppy1.setText("Empty");
                            jFloppy1.setToolTipText("Empty");                            
                            utils.Config.bFlop1Inserted=false;
                            Arrays.fill(m.floppyA.dimage,(byte)0);
                        } else {
                            jFloppy1.setText(utils.Config.strFlop1FilePath);
                            jFloppy1.setToolTipText(utils.Config.strFlop1FilePath);                            
                            if(utils.Config.bFlop1RW){
                                jFloppyButton1.setIcon(icoFloppyRW);
                            }
                            is.read(m.floppyA.dimage);
                            is.close();
                            utils.Config.bFlop1Inserted=true;
                        }
                    } catch (IOException ex) {
                    }
                
            } catch (FileNotFoundException ex) {

            }
           }
        }
        if (utils.Config.strFlop2FilePath.isEmpty()) {
            utils.Config.bFlop2Inserted=false;
        } else {
            if(utils.Config.bFlop2Inserted){
            try {
                InputStream is;
                is = new DataInputStream(new FileInputStream(utils.Config.strFlop2FilePath));
                    try {
                        if (is.available() != 256256) {
                            JOptionPane.showMessageDialog(null, "Invalid size of floppy image "+new File(utils.Config.strFlop2FilePath).getName()+"\nThe allowed size is 256256 bytes", "Error", JOptionPane.ERROR_MESSAGE);
                            jFloppy2.setText("Empty");
                            jFloppy2.setToolTipText("Empty");
                            utils.Config.bFlop2Inserted=false;
                            Arrays.fill(m.floppyB.dimage,(byte)0);                            
                        } else {
                            jFloppy2.setText(utils.Config.strFlop2FilePath);
                            jFloppy2.setToolTipText(utils.Config.strFlop2FilePath);
                            if(utils.Config.bFlop2RW){
                                jFloppyButton2.setIcon(icoFloppyRW);
                            }
                            is.read(m.floppyB.dimage);
                            is.close();   
                            utils.Config.bFlop2Inserted=true;
                        }
                    } catch (IOException ex) {
                    }
                
            } catch (FileNotFoundException ex) {

            }
           }
        }
        if(m.floppyCtrl!=null){
            m.floppyCtrl.I8272reset();
        }
        utils.Config.SaveConfig();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        fc = new javax.swing.JFileChooser();
        jToolBar1 = new javax.swing.JToolBar();
        jResetIco = new javax.swing.JButton();
        jPause = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jLoadMem = new javax.swing.JButton();
        jSaveMem = new javax.swing.JButton();
        jDebugger = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jSettings = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        jFloppyButton1 = new javax.swing.JButton();
        jFloppy1 = new javax.swing.JLabel();
        jFloppyButton2 = new javax.swing.JButton();
        jFloppy2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mLoad = new javax.swing.JMenuItem();
        mSave = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jScreenshot = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mExit = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenu9 = new javax.swing.JMenu();
        mPlay = new javax.swing.JRadioButtonMenuItem();
        mRecord = new javax.swing.JRadioButtonMenuItem();
        mInvert = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuCopy = new javax.swing.JMenuItem();
        jMenuPaste = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jCheckBoxSpeed05 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxspeed1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxSpeed2 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxSpeed3 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxSpeed4 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxSpeed5 = new javax.swing.JCheckBoxMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mSettings = new javax.swing.JMenuItem();
        jAbout = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jIQ151");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar1.setRollover(true);
        jToolBar1.setPreferredSize(new java.awt.Dimension(100, 20));

        jResetIco.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/reset.png"))); // NOI18N
        jResetIco.setToolTipText("Reset");
        jResetIco.setFocusable(false);
        jResetIco.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jResetIco.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jResetIco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jResetIcoActionPerformed(evt);
            }
        });
        jToolBar1.add(jResetIco);

        jPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pause.png"))); // NOI18N
        jPause.setToolTipText("Pause");
        jPause.setFocusable(false);
        jPause.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPause.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPauseActionPerformed1(evt);
            }
        });
        jToolBar1.add(jPause);
        jToolBar1.add(jSeparator3);

        jLoadMem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/binaryopn.png"))); // NOI18N
        jLoadMem.setToolTipText("Load Memory Block");
        jLoadMem.setFocusable(false);
        jLoadMem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLoadMem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jLoadMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadMemActionPerformed(evt);
            }
        });
        jToolBar1.add(jLoadMem);

        jSaveMem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/binarysav.png"))); // NOI18N
        jSaveMem.setToolTipText("Save Memory Block");
        jSaveMem.setFocusable(false);
        jSaveMem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSaveMem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSaveMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveMemActionPerformed(evt);
            }
        });
        jToolBar1.add(jSaveMem);

        jDebugger.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/debugger.png"))); // NOI18N
        jDebugger.setToolTipText("Debugger");
        jDebugger.setFocusable(false);
        jDebugger.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jDebugger.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jDebugger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDebuggerActionPerformed1(evt);
            }
        });
        jToolBar1.add(jDebugger);
        jToolBar1.add(jSeparator6);

        jSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/settings.png"))); // NOI18N
        jSettings.setToolTipText("Settings");
        jSettings.setFocusable(false);
        jSettings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSettings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSettingsActionPerformed(evt);
            }
        });
        jToolBar1.add(jSettings);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        statusPanel.setMaximumSize(new java.awt.Dimension(100, 32767));
        statusPanel.setMinimumSize(new java.awt.Dimension(100, 20));
        statusPanel.setPreferredSize(new java.awt.Dimension(100, 22));

        jFloppyButton1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jFloppyButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/floppy.png"))); // NOI18N
        jFloppyButton1.setText("0");
        jFloppyButton1.setToolTipText("Floppy Drive A");
        jFloppyButton1.setBorderPainted(false);
        jFloppyButton1.setFocusable(false);
        jFloppyButton1.setIconTextGap(1);
        jFloppyButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFloppyButton1ActionPerformed(evt);
            }
        });
        statusPanel.add(jFloppyButton1);

        jFloppy1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jFloppy1.setText("Empty");
        jFloppy1.setPreferredSize(new java.awt.Dimension(200, 14));
        statusPanel.add(jFloppy1);

        jFloppyButton2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jFloppyButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/floppy.png"))); // NOI18N
        jFloppyButton2.setText("1");
        jFloppyButton2.setToolTipText("Floppy Drive B");
        jFloppyButton2.setFocusable(false);
        jFloppyButton2.setIconTextGap(1);
        jFloppyButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFloppyButton2ActionPerformed(evt);
            }
        });
        statusPanel.add(jFloppyButton2);

        jFloppy2.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jFloppy2.setText("Empty");
        jFloppy2.setPreferredSize(new java.awt.Dimension(200, 14));
        statusPanel.add(jFloppy2);

        getContentPane().add(statusPanel, java.awt.BorderLayout.PAGE_END);

        jMenuBar1.setMaximumSize(new java.awt.Dimension(282, 500));

        jMenu1.setText("File");

        mLoad.setText("Open LOAD tape");
        mLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mLoadActionPerformed(evt);
            }
        });
        jMenu1.add(mLoad);

        mSave.setText("Open SAVE tape");
        mSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSaveActionPerformed(evt);
            }
        });
        jMenu1.add(mSave);
        jMenu1.add(jSeparator4);

        jMenuItem1.setText("Load Memory Block");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadMemoryBlockActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Save Memory Block");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveMemoryBlockActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);
        jMenu1.add(jSeparator1);

        jScreenshot.setText("Save screenshot");
        jScreenshot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jScreenshotActionPerformed(evt);
            }
        });
        jMenu1.add(jScreenshot);
        jMenu1.add(jSeparator7);

        mExit.setText("Exit");
        mExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mExitActionPerformed(evt);
            }
        });
        jMenu1.add(mExit);

        jMenuBar1.add(jMenu1);

        jMenu8.setText("Control");

        jMenu9.setText("Tape recorder");

        buttonGroup1.add(mPlay);
        mPlay.setSelected(true);
        mPlay.setText("Play");
        mPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RecorderAction(evt);
            }
        });
        jMenu9.add(mPlay);

        buttonGroup1.add(mRecord);
        mRecord.setForeground(java.awt.Color.red);
        mRecord.setText("Record");
        mRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RecorderAction(evt);
            }
        });
        jMenu9.add(mRecord);

        mInvert.setSelected(true);
        mInvert.setText("Invert signal");
        mInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mInvertActionPerformed(evt);
            }
        });
        jMenu9.add(mInvert);

        jMenu8.add(jMenu9);
        jMenu8.add(jSeparator2);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        jMenuItem5.setForeground(java.awt.Color.red);
        jMenuItem5.setText("Reset");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jResetActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem5);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Pause");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPauseActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem3);

        jMenuCopy.setText("Copy screen to clipboard");
        jMenuCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCopyActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuCopy);

        jMenuPaste.setText("Paste from clipboard");
        jMenuPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuPasteActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuPaste);

        jMenu3.setText("Speed");

        jCheckBoxSpeed05.setSelected(true);
        jCheckBoxSpeed05.setText("0,5x");
        jCheckBoxSpeed05.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSpeed05ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxSpeed05);

        jCheckBoxspeed1.setSelected(true);
        jCheckBoxspeed1.setText("1x");
        jCheckBoxspeed1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxspeed1ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxspeed1);

        jCheckBoxSpeed2.setSelected(true);
        jCheckBoxSpeed2.setText("2x");
        jCheckBoxSpeed2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSpeed2ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxSpeed2);

        jCheckBoxSpeed3.setSelected(true);
        jCheckBoxSpeed3.setText("4x");
        jCheckBoxSpeed3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSpeed3ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxSpeed3);

        jCheckBoxSpeed4.setSelected(true);
        jCheckBoxSpeed4.setText("10x");
        jCheckBoxSpeed4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSpeed4ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxSpeed4);

        jCheckBoxSpeed5.setSelected(true);
        jCheckBoxSpeed5.setText("40x");
        jCheckBoxSpeed5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSpeed5ActionPerformed(evt);
            }
        });
        jMenu3.add(jCheckBoxSpeed5);

        jMenu8.add(jMenu3);

        jMenuBar1.add(jMenu8);

        jMenu2.setText("Tools");

        jMenuItem4.setText("Debugger");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDebuggerActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);
        jMenu2.add(jSeparator5);

        mSettings.setText("Settings");
        mSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSettingsActionPerformed(evt);
            }
        });
        jMenu2.add(mSettings);

        jMenuBar1.add(jMenu2);

        jAbout.setText("About");
        jAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jAboutClicked(evt);
            }
        });
        jMenuBar1.add(jAbout);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mLoadActionPerformed
        boolean pau = m.isPaused();
        
        m.stopEmulation();

        fc.setDialogTitle("Open LOAD tape");
        fc.setFileFilter(loadFlt);
        int val = fc.showOpenDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            try {
                m.openLoadTape(fc.getSelectedFile().getCanonicalPath());
            } catch (IOException ex) {

            }
        }
        
        if (!pau) {
            
            m.startEmulation();
        }             
    }//GEN-LAST:event_mLoadActionPerformed

    private void mSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSaveActionPerformed
       boolean pau = m.isPaused();
        
        m.stopEmulation();
        
        fc.setDialogTitle("Open SAVE tape");
        fc.setFileFilter(saveFlt);
        int val = fc.showSaveDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            try {
                String s = fc.getSelectedFile().getCanonicalPath();
                if (!s.endsWith(".csw")) { s = s + ".csw"; }
                m.openSaveTape(s);
            } catch (IOException ex) {

            }
        }
        
        if (!pau) {
            
            m.startEmulation();
        }
    }//GEN-LAST:event_mSaveActionPerformed

    private void mExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mExitActionPerformed
        if(m.getSDRom()!=null){
            m.getSDRom().stopThread();
        }
        System.exit(0);
    }//GEN-LAST:event_mExitActionPerformed

    private void RecorderAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RecorderAction
        m.setTapeMode("Record".equals(evt.getActionCommand()));
    }//GEN-LAST:event_RecorderAction

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        m.shutdownCleanup();
    }//GEN-LAST:event_formWindowClosing

    private void mInvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mInvertActionPerformed
        m.setTapeInvert(mInvert.isSelected());
    }//GEN-LAST:event_mInvertActionPerformed

    private void mSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSettingsActionPerformed
        boolean pau = m.isPaused();
        
        m.stopEmulation();
        
        Settings set = new Settings();
        set.setSize(new Dimension(455, 470));
        set.setLocationRelativeTo(this);
        set.showDialog(m.getConfig());
        if (set.isResetNeeded()) {
            m.Reset(false);
            m.clearScreen();
        }
        Config cfg=m.getConfig();
        utils.Config.mainmodule=cfg.getMain();
        utils.Config.monitor=cfg.getMonitor();
        utils.Config.grafik=cfg.getGrafik();
        utils.Config.sdrom=cfg.getSDRom();     
        utils.Config.sdromautorun=cfg.getSDRomAutorun();
        utils.Config.felautorun=cfg.getFelAutorun();
        utils.Config.amosautorun=cfg.getAmosAutorun();        
        utils.Config.audio=cfg.getAudio();
        utils.Config.bDisc2=cfg.getDisc2();
        utils.Config.smartkeyboard=cfg.getSmartKbd();
              
        utils.Config.mem64=cfg.getMem64();
        utils.Config.video64=cfg.getVideo();
        utils.Config.SaveConfig();
        pack();
        set.dispose();
        if (cfg.getSDRom()) {
            addLEDbar();
        } else {
            removeLEDbar();
        }
        if(cfg.disc2){
            floppyON();
        }else{
            floppyOFF();
        } 
        if (!pau) {
            
            m.startEmulation();
        }
    }//GEN-LAST:event_mSettingsActionPerformed

    private void jAboutClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jAboutClicked
        JDialog dAbout = new About(new JFrame());     
        dAbout.setLocationRelativeTo(this);
        dAbout.setVisible(true);
    }//GEN-LAST:event_jAboutClicked

    private void jLoadMemoryBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadMemoryBlockActionPerformed
        boolean pau = m.isPaused();
        
        m.stopEmulation();
        bopn.setLocationRelativeTo(this);
        bopn.showDialog();
        bopn.setAlwaysOnTop(true); 
    }//GEN-LAST:event_jLoadMemoryBlockActionPerformed

    private void jSaveMemoryBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveMemoryBlockActionPerformed
       boolean pau = m.isPaused();
        
       m.stopEmulation();
        bsav.setLocationRelativeTo(this);
        bsav.showDialog(); 
        bsav.setAlwaysOnTop(true); 
    }//GEN-LAST:event_jSaveMemoryBlockActionPerformed

    private void jDebuggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDebuggerActionPerformed
        boolean pau = m.isPaused();
        
        m.stopEmulation();
        deb.showDialog(); 
        deb.setAlwaysOnTop(true);
    }//GEN-LAST:event_jDebuggerActionPerformed

    public void setPauseIcon(boolean bWhatIco) {
        if (bWhatIco) {
            jPause.setIcon(icoPause);
        } else {
            jPause.setIcon(icoRun);
        }
    }

    
    private void jResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jResetActionPerformed
        if(m.isPaused()){
            
            m.startEmulation();
        }
        floppyUpdate();
        m.Reset(true);
        m.clearScreen();
    }//GEN-LAST:event_jResetActionPerformed

    private void jPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPauseActionPerformed
        if(m.isPaused()){
            
            m.startEmulation();
        }else{
            
            m.stopEmulation();
        }
    }//GEN-LAST:event_jPauseActionPerformed

    private void jResetIcoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jResetIcoActionPerformed
        jResetActionPerformed(null);
    }//GEN-LAST:event_jResetIcoActionPerformed

    private void jPauseActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPauseActionPerformed1
        jPauseActionPerformed(null);
    }//GEN-LAST:event_jPauseActionPerformed1

    private void jDebuggerActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDebuggerActionPerformed1
        boolean pau = m.isPaused();
        
        m.stopEmulation();
        deb.showDialog(); 
        deb.setAlwaysOnTop(true);
    }//GEN-LAST:event_jDebuggerActionPerformed1

    private void jSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSettingsActionPerformed
        mSettingsActionPerformed(null);
    }//GEN-LAST:event_jSettingsActionPerformed

    private void jLoadMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadMemActionPerformed
       jLoadMemoryBlockActionPerformed(null);
    }//GEN-LAST:event_jLoadMemActionPerformed

    private void jSaveMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveMemActionPerformed
       jSaveMemoryBlockActionPerformed(null);
    }//GEN-LAST:event_jSaveMemActionPerformed
  
    public String getExtension(String fileName) {
        String extension = "";
        if (fileName != null) {
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1);
            }
        }
        return extension;
    }
    
    public String removeExtension(String fileName) {
        String strNewFileName = "";
        String strTemp;
        if (fileName != null) {
            int i = fileName.lastIndexOf(File.separator);
            if (i > 0) {
                strNewFileName = fileName.substring(0, i+1);
                strTemp = fileName.substring(i + 1);

            } else {
                strNewFileName = "";
                strTemp = fileName;
            }
            i = strTemp.lastIndexOf('.');
            if (i > 0) {
                strTemp = strTemp.substring(0, i);
            }
            strNewFileName += strTemp;
        }
        return strNewFileName;
    }
    
    
    //vlozi do jmena souboru index pokud tam neni, pote vrati dalsi jmeno v poradi indexu
    private String getNextFileName(String strCurrentFileName, String strDefault) {
        String strRet = strDefault;
        String strShort=new File(strCurrentFileName).getName();
        String strExtension=getExtension(strShort);
        if(!strExtension.isEmpty()){
            strExtension="."+strExtension;
        }
        if (!strShort.isEmpty()) {
            String strTmpNoExt = strShort;
            if(!getExtension(strShort).isEmpty()){
             strTmpNoExt = strShort.substring(0, strShort.lastIndexOf('.'));
            }
            Pattern pattern = Pattern.compile("^(.*\\D)(\\d+)$");
            Matcher matcher = pattern.matcher(strTmpNoExt);
            if (matcher.find()) { 
                int nFmt=matcher.group(2).length();
                int nNewVal=Integer.parseInt(matcher.group(2))+1;                
                strRet=matcher.group(1)+String.format("%0"+nFmt+"d", nNewVal)+strExtension;
            }else{
                strRet=strTmpNoExt+"01"+strExtension;
            }
        }
        return strRet;
    }  
    
    private void jScreenshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jScreenshotActionPerformed

        BufferedImage shot = m.getImage();
        boolean pau = m.isPaused();
        m.stopEmulation();

        fc.setDialogTitle("Save screenshot");
        utils.Config.LoadConfig();         
        fc.setCurrentDirectory(new File(utils.Config.nullToEmpty(new File(utils.Config.strShotFilePath).getParent())));        
        String strNewFileName=utils.Config.strShotFilePath;
        fc.setSelectedFile(new File(removeExtension(getNextFileName(strNewFileName,"screen01"))));
        fc.resetChoosableFileFilters();
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileNameExtensionFilter("IQ151 screenshots", "png"));
        int val = fc.showSaveDialog(this);

        if (val == JFileChooser.APPROVE_OPTION) {
            try {
                String strSnap = fc.getSelectedFile().getCanonicalPath();
                if (!getExtension(strSnap).equalsIgnoreCase("png")) {
                    strSnap += ".png";
                }
                File outputfile = new File(strSnap);
                try {
                    ImageIO.write(shot, "png", outputfile);
                } catch (IOException ex) {
                    Logger.getLogger(JIQ151.class.getName()).log(Level.SEVERE, null, ex);
                } 
                utils.Config.strShotFilePath = strSnap;
                utils.Config.SaveConfig();
            } catch (IOException ex) {
                Logger.getLogger(JIQ151.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!pau) {
            m.startEmulation();
        }
    }//GEN-LAST:event_jScreenshotActionPerformed

    private void jFloppyButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFloppyButton1ActionPerformed
        
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open floppy image");
        fc.resetChoosableFileFilters();
        fc.setCurrentDirectory(new File(utils.Config.strFlop1FilePath));
        fc.setAcceptAllFileFilterUsed(true);
        JPanel panel1 = (JPanel)fc.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        JButton btnEmpty=new JButton("Eject disk");
        btnEmpty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent action) {                
               fc.cancelSelection();               
               utils.Config.bFlop1Inserted=false;
               utils.Config.SaveConfig();
               floppyUpdate();
            }
        });
        JCheckBox chckWrites=new JCheckBox("R/W mode");
        Component c1=panel2.getComponent(0);
        Component c2=panel2.getComponent(1);
        panel2.removeAll();
        
        panel2.add(chckWrites);
        panel2.add(c1);
        panel2.add(btnEmpty);
        panel2.add(c2);
        int val = fc.showOpenDialog(this);
        
        if (val==JFileChooser.APPROVE_OPTION) {
            try {
                utils.Config.strFlop1FilePath=fc.getSelectedFile().getCanonicalPath();
                utils.Config.bFlop1RW=chckWrites.isSelected();
                utils.Config.bFlop1Inserted=true;
                utils.Config.SaveConfig();                 
                floppyUpdate();
            } catch (IOException ex) {
               
            }
        }       
    }//GEN-LAST:event_jFloppyButton1ActionPerformed

    private void jFloppyButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFloppyButton2ActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open floppy image");
        fc.resetChoosableFileFilters();
        fc.setCurrentDirectory(new File(utils.Config.strFlop2FilePath));
        fc.setAcceptAllFileFilterUsed(true);
        JPanel panel1 = (JPanel)fc.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        JButton btnEmpty=new JButton("Eject disk");
         btnEmpty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent action) {                
               fc.cancelSelection();               
               utils.Config.bFlop2Inserted=false;
               utils.Config.SaveConfig(); 
               floppyUpdate();
            }
        });
        JCheckBox chckWrites=new JCheckBox("R/W mode");
        Component c1=panel2.getComponent(0);
        Component c2=panel2.getComponent(1);
        panel2.removeAll();
        
        panel2.add(chckWrites);
        panel2.add(c1);
        panel2.add(btnEmpty);
        panel2.add(c2);
        int val = fc.showOpenDialog(this);
        
        if (val==JFileChooser.APPROVE_OPTION) {
            try {
                utils.Config.strFlop2FilePath=fc.getSelectedFile().getCanonicalPath();
                utils.Config.bFlop2RW=chckWrites.isSelected();
                utils.Config.bFlop2Inserted=true;
                utils.Config.SaveConfig(); 
                floppyUpdate();
            } catch (IOException ex) {
               
            }
        }
    }//GEN-LAST:event_jFloppyButton2ActionPerformed

    private void jCheckBoxSpeed05ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSpeed05ActionPerformed
        jCheckBoxSpeed05.setSelected(true);
        jCheckBoxspeed1.setSelected(false);
        jCheckBoxSpeed2.setSelected(false);
        jCheckBoxSpeed3.setSelected(false);
        jCheckBoxSpeed4.setSelected(false);
        jCheckBoxSpeed5.setSelected(false);
        m.setSpeed(1);
    }//GEN-LAST:event_jCheckBoxSpeed05ActionPerformed

    private void jCheckBoxspeed1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxspeed1ActionPerformed
        jCheckBoxSpeed05.setSelected(false);
        jCheckBoxspeed1.setSelected(true);
        jCheckBoxSpeed2.setSelected(false);
        jCheckBoxSpeed3.setSelected(false);
        jCheckBoxSpeed4.setSelected(false);
        jCheckBoxSpeed5.setSelected(false);
        m.setSpeed(2);
    }//GEN-LAST:event_jCheckBoxspeed1ActionPerformed

    private void jCheckBoxSpeed2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSpeed2ActionPerformed
        jCheckBoxSpeed05.setSelected(false);
        jCheckBoxspeed1.setSelected(false);
        jCheckBoxSpeed2.setSelected(true);
        jCheckBoxSpeed3.setSelected(false);
        jCheckBoxSpeed4.setSelected(false);
        jCheckBoxSpeed5.setSelected(false);
        m.setSpeed(4);
    }//GEN-LAST:event_jCheckBoxSpeed2ActionPerformed

    private void jCheckBoxSpeed3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSpeed3ActionPerformed
        jCheckBoxSpeed05.setSelected(false);
        jCheckBoxspeed1.setSelected(false);
        jCheckBoxSpeed2.setSelected(false);
        jCheckBoxSpeed3.setSelected(true);
        jCheckBoxSpeed4.setSelected(false);
        jCheckBoxSpeed5.setSelected(false);
        m.setSpeed(8);
    }//GEN-LAST:event_jCheckBoxSpeed3ActionPerformed

    private void jMenuPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuPasteActionPerformed
       m.scr.PasteFromClip();
    }//GEN-LAST:event_jMenuPasteActionPerformed

    private void jCheckBoxSpeed4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSpeed4ActionPerformed
        jCheckBoxSpeed05.setSelected(false);
        jCheckBoxspeed1.setSelected(false);
        jCheckBoxSpeed2.setSelected(false);
        jCheckBoxSpeed3.setSelected(false);
        jCheckBoxSpeed4.setSelected(true);
        jCheckBoxSpeed5.setSelected(false);
        m.setSpeed(20);
    }//GEN-LAST:event_jCheckBoxSpeed4ActionPerformed

    private void jCheckBoxSpeed5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSpeed5ActionPerformed
        jCheckBoxSpeed05.setSelected(false);
        jCheckBoxspeed1.setSelected(false);
        jCheckBoxSpeed2.setSelected(false);
        jCheckBoxSpeed3.setSelected(false);
        jCheckBoxSpeed4.setSelected(false);
        jCheckBoxSpeed5.setSelected(true);
        m.setSpeed(80);
    }//GEN-LAST:event_jCheckBoxSpeed5ActionPerformed

    private void jMenuCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCopyActionPerformed
        String strClipContent = "";
        byte vm[][] = m.getVideoMemory();
        int nEosCnt;
        int nPg = 1;
        int nEos = 32;
        if (m.getConfig().getVideo() == m.getConfig().VIDEO64) {
            nPg = 2;
            nEos = 64;
        }
        nEosCnt = nEos;
        for (int j = 0; j < nPg; j++) {
            for (int i = 0; i < 1024; i++) {
                strClipContent += (char)((vm[j][i] & 0x7f) > 32 ? (vm[j][i] & 0x7f) : 32);
                nEosCnt--;
                if (nEosCnt == 0) {
                    strClipContent += (char) 10;
                    nEosCnt = nEos;
                }
            }
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection strSel = new StringSelection(strClipContent);
        clipboard.setContents(strSel, null);
    }//GEN-LAST:event_jMenuCopyActionPerformed

    private void initEmulator() {
        m = new Iq();
        scr = new JIQScreen();
        scr.setMachine(m);
        m.setScreen(scr);
        scr.setImage(m.getImage());
        deb=new Debugger(m);        
        bopn=new BinOpen(m);
        bsav=new BinSave(m);
        m.setDebugger(deb); 
        m.setFrame(this);        
        getContentPane().add(scr, BorderLayout.CENTER);
        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width-getSize().width)/2, (screen.height-getSize().height)/2);
        addKeyListener(m.getKeyboard());
        m.Reset(true);
        m.start();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JIQ151.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JIQ151.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JIQ151.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JIQ151.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new JIQ151().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JFileChooser fc;
    private javax.swing.JMenu jAbout;
    private javax.swing.JCheckBoxMenuItem jCheckBoxSpeed05;
    private javax.swing.JCheckBoxMenuItem jCheckBoxSpeed2;
    private javax.swing.JCheckBoxMenuItem jCheckBoxSpeed3;
    private javax.swing.JCheckBoxMenuItem jCheckBoxSpeed4;
    private javax.swing.JCheckBoxMenuItem jCheckBoxSpeed5;
    private javax.swing.JCheckBoxMenuItem jCheckBoxspeed1;
    private javax.swing.JButton jDebugger;
    private javax.swing.JLabel jFloppy1;
    private javax.swing.JLabel jFloppy2;
    private javax.swing.JButton jFloppyButton1;
    private javax.swing.JButton jFloppyButton2;
    private javax.swing.JButton jLoadMem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuCopy;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuPaste;
    private javax.swing.JButton jPause;
    private javax.swing.JButton jResetIco;
    private javax.swing.JButton jSaveMem;
    private javax.swing.JMenuItem jScreenshot;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JButton jSettings;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem mExit;
    private javax.swing.JCheckBoxMenuItem mInvert;
    private javax.swing.JMenuItem mLoad;
    private javax.swing.JRadioButtonMenuItem mPlay;
    private javax.swing.JRadioButtonMenuItem mRecord;
    private javax.swing.JMenuItem mSave;
    private javax.swing.JMenuItem mSettings;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

}
