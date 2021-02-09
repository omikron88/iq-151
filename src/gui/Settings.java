/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import machine.Config;

/**
 *
 * @author admin
 */
public class Settings extends javax.swing.JDialog {

    Config cf;
    boolean ResetNeeded;
    
    /**
     * Creates new form Settings
     */
    public Settings() {
        initComponents();
        setIconImage((new ImageIcon(getClass().getResource("/icons/settings.png")).getImage()));
        ResetNeeded = false;
    }
    
    public boolean isResetNeeded() {
        return ResetNeeded;
    }
    
    public void showDialog(Config conf) {
        cf = conf;
       

         if (cf.grafik) {
            bGrafik.setSelected(true);
        }
         else {
            bGrafik.setSelected(false);            
        }
         if (cf.sdrom) {
            bSDROM.setSelected(true);
            jAutorun.setEnabled(true);
        }
         else {
            bSDROM.setSelected(false); 
            jAutorun.setEnabled(false);
        }
        if (cf.sdromautorun) {
            jAutorun.setSelected(true);
        }
         else {
            jAutorun.setSelected(false);            
        }
        if (cf.audio) {
            bAudio.setSelected(true);
        }
         else {
            bAudio.setSelected(false);            
        }
        if (cf.getMem64()) {
            b64KB.setSelected(true);
        }
        else {
            b32KB.setSelected(true);            
        }
        
        if (cf.getVideo()==cf.VIDEO64) {
            b64.setSelected(true);
        }
        else {
            b32.setSelected(true);            
        }

        switch(cf.getMain()) {
            case 0: {
                bNone.setSelected(true);
                break;
            }
            case 1: {
                bBasic6.setSelected(true);
                break;
            }
            case 2: {
                bBasicG.setSelected(true);
                break;
            }
            case 3: {
                bAmos.setSelected(true);
                break;
            }
        }
        switch(cf.getMonitor()) {
            case 10: {
                bStandard.setSelected(true);
                break;
            }
            case 11: {
                bDisassembler.setSelected(true);
                break;
            }
            case 12: {
                bCPMkom.setSelected(true);
                break;
            }
            case 13: {
                bCPMfel.setSelected(true);
                break;
            }
        }
        ResetNeeded = false;
        setModal(true);
        setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        RamGroup = new javax.swing.ButtonGroup();
        VideoGroup = new javax.swing.ButtonGroup();
        MainGroup = new javax.swing.ButtonGroup();
        MonitorGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        RamPanel = new javax.swing.JPanel();
        b32KB = new javax.swing.JRadioButton();
        b64KB = new javax.swing.JRadioButton();
        VideoPanel = new javax.swing.JPanel();
        b32 = new javax.swing.JRadioButton();
        b64 = new javax.swing.JRadioButton();
        MainPanel = new javax.swing.JPanel();
        bNone = new javax.swing.JRadioButton();
        bBasic6 = new javax.swing.JRadioButton();
        bBasicG = new javax.swing.JRadioButton();
        bAmos = new javax.swing.JRadioButton();
        MonitorPanel = new javax.swing.JPanel();
        bStandard = new javax.swing.JRadioButton();
        bDisassembler = new javax.swing.JRadioButton();
        bCPMkom = new javax.swing.JRadioButton();
        bCPMfel = new javax.swing.JRadioButton();
        AuxPanel = new javax.swing.JPanel();
        bGrafik = new javax.swing.JCheckBox();
        bSDROM = new javax.swing.JCheckBox();
        jAutorun = new javax.swing.JCheckBox();
        bOk = new javax.swing.JButton();
        bAudio = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");
        setModal(true);
        setName("SettingsDlg"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel1.setPreferredSize(new java.awt.Dimension(310, 290));

        RamPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("RAM Size"));

        RamGroup.add(b32KB);
        b32KB.setText("32 kB");
        b32KB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b32KBActionPerformed(evt);
            }
        });

        RamGroup.add(b64KB);
        b64KB.setText("64 kB");
        b64KB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b64KBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RamPanelLayout = new javax.swing.GroupLayout(RamPanel);
        RamPanel.setLayout(RamPanelLayout);
        RamPanelLayout.setHorizontalGroup(
            RamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RamPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(b32KB)
                    .addComponent(b64KB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        RamPanelLayout.setVerticalGroup(
            RamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RamPanelLayout.createSequentialGroup()
                .addComponent(b32KB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(b64KB))
        );

        VideoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Video"));

        VideoGroup.add(b32);
        b32.setText("32");
        b32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b32ActionPerformed(evt);
            }
        });

        VideoGroup.add(b64);
        b64.setText("64");
        b64.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b64ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout VideoPanelLayout = new javax.swing.GroupLayout(VideoPanel);
        VideoPanel.setLayout(VideoPanelLayout);
        VideoPanelLayout.setHorizontalGroup(
            VideoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(VideoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(b64)
                    .addComponent(b32))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        VideoPanelLayout.setVerticalGroup(
            VideoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VideoPanelLayout.createSequentialGroup()
                .addComponent(b32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(b64))
        );

        MainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Main module"));

        MainGroup.add(bNone);
        bNone.setText("None");
        bNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNoneActionPerformed(evt);
            }
        });

        MainGroup.add(bBasic6);
        bBasic6.setText("Basic 6");
        bBasic6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBasic6ActionPerformed(evt);
            }
        });

        MainGroup.add(bBasicG);
        bBasicG.setText("Basic G");
        bBasicG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBasicGActionPerformed(evt);
            }
        });

        MainGroup.add(bAmos);
        bAmos.setText("Amos (Ass+Pas)");
        bAmos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAmosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MainPanelLayout = new javax.swing.GroupLayout(MainPanel);
        MainPanel.setLayout(MainPanelLayout);
        MainPanelLayout.setHorizontalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bBasicG)
                    .addComponent(bBasic6)
                    .addComponent(bNone)
                    .addComponent(bAmos))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        MainPanelLayout.setVerticalGroup(
            MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainPanelLayout.createSequentialGroup()
                .addComponent(bNone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bBasic6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bBasicG, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bAmos, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        MonitorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitor"));

        MonitorGroup.add(bStandard);
        bStandard.setText("Standard");
        bStandard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStandardActionPerformed(evt);
            }
        });

        MonitorGroup.add(bDisassembler);
        bDisassembler.setText("Disassembler");
        bDisassembler.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDisassemblerActionPerformed(evt);
            }
        });

        MonitorGroup.add(bCPMkom);
        bCPMkom.setText("CP/M Komenium");
        bCPMkom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCPMkomActionPerformed(evt);
            }
        });

        MonitorGroup.add(bCPMfel);
        bCPMfel.setText("CP/M FEL");
        bCPMfel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCPMfelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MonitorPanelLayout = new javax.swing.GroupLayout(MonitorPanel);
        MonitorPanel.setLayout(MonitorPanelLayout);
        MonitorPanelLayout.setHorizontalGroup(
            MonitorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MonitorPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(MonitorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bCPMkom)
                    .addComponent(bDisassembler)
                    .addComponent(bStandard)
                    .addComponent(bCPMfel)))
        );
        MonitorPanelLayout.setVerticalGroup(
            MonitorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MonitorPanelLayout.createSequentialGroup()
                .addComponent(bStandard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bDisassembler)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bCPMkom, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bCPMfel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        AuxPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Aux module"));

        bGrafik.setText("Grafik");
        bGrafik.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bGrafikStateChanged(evt);
            }
        });

        bSDROM.setText("SD-ROM");
        bSDROM.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bSDROMItemStateChanged(evt);
            }
        });

        jAutorun.setText("Autorun");
        jAutorun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAutorunActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout AuxPanelLayout = new javax.swing.GroupLayout(AuxPanel);
        AuxPanel.setLayout(AuxPanelLayout);
        AuxPanelLayout.setHorizontalGroup(
            AuxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AuxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AuxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bGrafik)
                    .addGroup(AuxPanelLayout.createSequentialGroup()
                        .addComponent(bSDROM)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAutorun)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        AuxPanelLayout.setVerticalGroup(
            AuxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AuxPanelLayout.createSequentialGroup()
                .addComponent(bGrafik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(AuxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bSDROM)
                    .addComponent(jAutorun))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        bOk.setText("Ok");
        bOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bOkActionPerformed(evt);
            }
        });

        bAudio.setText("Audio");
        bAudio.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bAudioItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(AuxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(60, 60, 60)
                                .addComponent(bOk))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bAudio))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(RamPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(MonitorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(VideoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(RamPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(VideoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(MonitorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(AuxPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(24, 24, 24))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(bAudio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addComponent(bOk)
                        .addGap(48, 48, 48))))
        );

        getContentPane().add(jPanel1);
        jPanel1.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void bOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOkActionPerformed
        setVisible(false);
        setModal(false);
    }//GEN-LAST:event_bOkActionPerformed

    private void bGrafikStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bGrafikStateChanged
        if (bGrafik.isSelected()){cf.grafik=true;}else{cf.grafik=false;};
        ResetNeeded = true;
    }//GEN-LAST:event_bGrafikStateChanged

    private void bAmosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAmosActionPerformed
        cf.setMain(cf.AMOS);
        ResetNeeded = true;
    }//GEN-LAST:event_bAmosActionPerformed

    private void bBasicGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBasicGActionPerformed
        cf.setMain(cf.BASICG);
        ResetNeeded = true;
    }//GEN-LAST:event_bBasicGActionPerformed

    private void bBasic6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBasic6ActionPerformed
        cf.setMain(cf.BASIC6);
        ResetNeeded = true;
    }//GEN-LAST:event_bBasic6ActionPerformed

    private void bNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNoneActionPerformed
        cf.setMain(cf.NONE);
        ResetNeeded = true;
    }//GEN-LAST:event_bNoneActionPerformed

    private void b64ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b64ActionPerformed
        cf.setVideo(cf.VIDEO64);
        ResetNeeded = true;
    }//GEN-LAST:event_b64ActionPerformed

    private void b32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b32ActionPerformed
        cf.setVideo(cf.VIDEO32);
        ResetNeeded = true;
    }//GEN-LAST:event_b32ActionPerformed

    private void b64KBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b64KBActionPerformed
        cf.setMem64(true);
        ResetNeeded = true;
    }//GEN-LAST:event_b64KBActionPerformed

    private void b32KBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b32KBActionPerformed
        cf.setMem64(false);
        ResetNeeded = true;
    }//GEN-LAST:event_b32KBActionPerformed

    private void bStandardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bStandardActionPerformed
        cf.setMonitor(cf.Mstandard);
        ResetNeeded = true;
    }//GEN-LAST:event_bStandardActionPerformed

    private void bDisassemblerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDisassemblerActionPerformed
        cf.setMonitor(cf.Mdisassembler);
        ResetNeeded = true;
    }//GEN-LAST:event_bDisassemblerActionPerformed

    private void bCPMkomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCPMkomActionPerformed
        cf.setMonitor(cf.MCPMkom);
        ResetNeeded = true;
    }//GEN-LAST:event_bCPMkomActionPerformed

    private void bCPMfelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCPMfelActionPerformed
        cf.setMonitor(cf.MCPMfel);
        ResetNeeded = true;
    }//GEN-LAST:event_bCPMfelActionPerformed

    private void bSDROMItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bSDROMItemStateChanged
        if (bSDROM.isSelected()){cf.sdrom=true;}else{cf.sdrom=false;};
        jAutorun.setEnabled(bSDROM.isSelected());
        ResetNeeded = true;
    }//GEN-LAST:event_bSDROMItemStateChanged

    private void bAudioItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bAudioItemStateChanged
        if(bAudio.isSelected()){cf.audio=true;}else{cf.audio=false;};
        ResetNeeded = true;
    }//GEN-LAST:event_bAudioItemStateChanged

    private void jAutorunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAutorunActionPerformed
        if (jAutorun.isSelected()){cf.sdromautorun=true;}else{cf.sdromautorun=false;};
        ResetNeeded = true;
    }//GEN-LAST:event_jAutorunActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AuxPanel;
    private javax.swing.ButtonGroup MainGroup;
    private javax.swing.JPanel MainPanel;
    private javax.swing.ButtonGroup MonitorGroup;
    private javax.swing.JPanel MonitorPanel;
    private javax.swing.ButtonGroup RamGroup;
    private javax.swing.JPanel RamPanel;
    private javax.swing.ButtonGroup VideoGroup;
    private javax.swing.JPanel VideoPanel;
    private javax.swing.JRadioButton b32;
    private javax.swing.JRadioButton b32KB;
    private javax.swing.JRadioButton b64;
    private javax.swing.JRadioButton b64KB;
    private javax.swing.JRadioButton bAmos;
    private javax.swing.JCheckBox bAudio;
    private javax.swing.JRadioButton bBasic6;
    private javax.swing.JRadioButton bBasicG;
    private javax.swing.JRadioButton bCPMfel;
    private javax.swing.JRadioButton bCPMkom;
    private javax.swing.JRadioButton bDisassembler;
    private javax.swing.JCheckBox bGrafik;
    private javax.swing.JRadioButton bNone;
    private javax.swing.JButton bOk;
    private javax.swing.JCheckBox bSDROM;
    private javax.swing.JRadioButton bStandard;
    private javax.swing.JCheckBox jAutorun;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
