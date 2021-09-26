/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import machine.Config;
import machine.Iq;

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

    /**
     * Creates new form JIQ151
     */
    public JIQ151() {
        initComponents();
        setIconImage((new ImageIcon(getClass().getResource("/icons/iq.png")).getImage()));
        //presun polozky menu About doprava
        jMenuBar1.remove(jMenu7);
        jMenuBar1.add(Box.createHorizontalGlue());
        jMenuBar1.add(jMenu7);        
        jToolBar1.setBackground(new Color(240,240,240));
        getContentPane().setBackground(new Color(240,240,240));
        //getContentPane().setBackground(Color.black);
        initEmulator();
        pack();
        if(utils.Config.sdrom){
            addLEDbar();
        }else{
            removeLEDbar();
        }
    }

    private void addLEDbar() {
        if (ledPanel == null) {
            lblLed = new JLabel("");
            lblLed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gray.png")));
            m.setSDRomLED(lblLed);
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(new Color(240,240,240));
            bottomPanel.add(lblLed, BorderLayout.LINE_END);
            ledPanel = new JPanel(new BorderLayout());
            ledPanel.setBackground(new Color(240,240,240));
            ledPanel.add(bottomPanel, BorderLayout.PAGE_END);
            this.getContentPane().add(ledPanel,BorderLayout.PAGE_END);             
        }        
        setSize(566, scr.getPreferredSize().height+lblLed.getPreferredSize().height+jToolBar1.getPreferredSize().height);
        scr.repaint();
    }
    
    private void removeLEDbar(){
        if(ledPanel!=null){
            this.getContentPane().remove(ledPanel);
            ledPanel=null;
        }
        setSize(566, scr.getPreferredSize().height+jToolBar1.getPreferredSize().height);
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
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mSettings = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jIQ151");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar1.setRollover(true);

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

        jMenuBar1.add(jMenu8);

        jMenu2.setText("Tools");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Debugger");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDebuggerActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);
        jMenu2.add(jSeparator5);

        mSettings.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mSettings.setText("Settings");
        mSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSettingsActionPerformed(evt);
            }
        });
        jMenu2.add(mSettings);

        jMenuBar1.add(jMenu2);

        jMenu7.setText("About");
        jMenu7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jAboutClicked(evt);
            }
        });
        jMenuBar1.add(jMenu7);

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
        set.setSize(388, 488);
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
        utils.Config.audio=cfg.getAudio();
              
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

    private void initEmulator() {
        m = new Iq();
        scr = new JIQScreen();       
        m.setScreen(scr);
        scr.setImage(m.getImage());
        deb=new Debugger(m);        
        bopn=new BinOpen(m);
        bsav=new BinSave(m);
        m.setDebugger(deb); 
        m.setFrame(this);
        getContentPane().add(scr, BorderLayout.LINE_START);
        pack();         
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
    private javax.swing.JButton jDebugger;
    private javax.swing.JButton jLoadMem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
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
    // End of variables declaration//GEN-END:variables

}
