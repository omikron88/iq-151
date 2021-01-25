/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    /**
     * Creates new form JIQ151
     */
    public JIQ151() {
        initComponents();
        initEmulator();
        if(utils.Config.sdrom){
            addLEDbar();
        }else{
            removeLEDbar();
        }
    }

    private void addLEDbar() {
        if (ledPanel == null) {
            lblLed = new JLabel("• ");
            lblLed.setForeground(Color.GRAY);
            m.setSDRomLED(lblLed);
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(lblLed, BorderLayout.LINE_END);
            ledPanel = new JPanel(new BorderLayout());
            ledPanel.add(bottomPanel, BorderLayout.PAGE_END);
            this.getContentPane().add(ledPanel);                       
        }
        setSize(566, 620);
    }
    
    private void removeLEDbar(){
        if(ledPanel!=null){
            this.getContentPane().remove(ledPanel);
            ledPanel=null;
        }
        setSize(566, 593);
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
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu6 = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        mLoad = new javax.swing.JMenuItem();
        mSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mSettings = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        mPlay = new javax.swing.JRadioButtonMenuItem();
        mRecord = new javax.swing.JRadioButtonMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mInvert = new javax.swing.JCheckBoxMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jIQ151");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jMenuBar1.setMaximumSize(new java.awt.Dimension(282, 500));

        jMenu6.setText("Reset");
        jMenu6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bResetActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu6);

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
        jMenu1.add(jSeparator1);

        mSettings.setText("Settings");
        mSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSettingsActionPerformed(evt);
            }
        });
        jMenu1.add(mSettings);
        jMenu1.add(jSeparator3);

        mExit.setText("Exit");
        mExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mExitActionPerformed(evt);
            }
        });
        jMenu1.add(mExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tape recorder");

        buttonGroup1.add(mPlay);
        mPlay.setSelected(true);
        mPlay.setText("Play");
        mPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RecorderAction(evt);
            }
        });
        jMenu2.add(mPlay);

        buttonGroup1.add(mRecord);
        mRecord.setForeground(java.awt.Color.red);
        mRecord.setText("Record");
        mRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RecorderAction(evt);
            }
        });
        jMenu2.add(mRecord);
        jMenu2.add(jSeparator2);

        mInvert.setSelected(true);
        mInvert.setText("Invert signal");
        mInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mInvertActionPerformed(evt);
            }
        });
        jMenu2.add(mInvert);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Debugger");
        jMenu3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bDebuggerActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu3);

        jMenu4.setText("Load");
        jMenu4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonBinaryOpenActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu4);

        jMenu5.setText("Save");
        jMenu5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonBinarySaveActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu5);

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
        
        if (!pau) m.startEmulation();
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
        
        if (!pau) m.startEmulation();
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
        set.showDialog(m.getConfig());
        if (set.isResetNeeded()) {
            m.Reset(false);
            m.clearScreen();
        }
        Config cfg=m.getConfig();
        utils.Config.mainmodule=cfg.getMain();
        utils.Config.grafik=cfg.getGrafik();
        utils.Config.sdrom=cfg.getSDRom();     
        if(cfg.getSDRom()){
            addLEDbar();
        }else{
            removeLEDbar();
        }       
        utils.Config.mem64=cfg.getMem64();
        utils.Config.video64=cfg.getVideo();
        utils.Config.SaveConfig();
                
        set.dispose();
        if (!pau) m.startEmulation();
    }//GEN-LAST:event_mSettingsActionPerformed

    private void bResetActionPerformed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bResetActionPerformed
            m.Reset(false);
            m.clearScreen(); 
            jMenu6.setSelected(false);
            this.requestFocusInWindow(); 
    }//GEN-LAST:event_bResetActionPerformed

    private void bDebuggerActionPerformed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bDebuggerActionPerformed
                
        boolean pau = m.isPaused();
        m.stopEmulation();
        deb.showDialog(); 
        deb.setAlwaysOnTop(true);
    }//GEN-LAST:event_bDebuggerActionPerformed

    private void jButtonBinaryOpenActionPerformed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBinaryOpenActionPerformed
        boolean pau = m.isPaused();
        m.stopEmulation();
        bopn.showDialog();
        bopn.setAlwaysOnTop(true);
    }//GEN-LAST:event_jButtonBinaryOpenActionPerformed

    private void jButtonBinarySaveActionPerformed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBinarySaveActionPerformed
        boolean pau = m.isPaused();
        m.stopEmulation();
        bsav.showDialog(); 
        bsav.setAlwaysOnTop(true);
    }//GEN-LAST:event_jButtonBinarySaveActionPerformed

    private void initEmulator() {
        m = new Iq();
        scr = new JIQScreen();
        m.setScreen(scr);
        scr.setImage(m.getImage());
        deb=new Debugger(m);        
        bopn=new BinOpen(m);
        bsav=new BinSave(m);
        m.setDebugger(deb);
        getContentPane().add(scr, BorderLayout.CENTER);
        pack();         
        addKeyListener(m.getKeyboard());
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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuItem mExit;
    private javax.swing.JCheckBoxMenuItem mInvert;
    private javax.swing.JMenuItem mLoad;
    private javax.swing.JRadioButtonMenuItem mPlay;
    private javax.swing.JRadioButtonMenuItem mRecord;
    private javax.swing.JMenuItem mSave;
    private javax.swing.JMenuItem mSettings;
    // End of variables declaration//GEN-END:variables

}
