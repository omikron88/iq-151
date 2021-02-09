/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BinOpen.java
 *
 * Created on Oct 7, 2019, 12:50:56 PM
 */
package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import machine.Iq;
import utils.Config;

/**
 *
 * @author Administrator
 */
public class BinOpen extends javax.swing.JFrame {
     private Iq m;
     
    /** Creates new form BinOpen */
    public BinOpen(Iq inM) {
        initComponents();
        setIconImage((new ImageIcon(getClass().getResource("/icons/binaryopn.png")).getImage()));
        Config.LoadConfig();
        m = inM;
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                m.startEmulation();
            }
        });
    }
    
    public void refreshDlg(){
      jTextBinFile.setText(Config.strBinFilePath);
      jNumberTextSavAdr.setText(String.format("%04X",Config.nBeginBinAddress));
      jCheckBox1.setSelected(Config.bRunBin);
      jNumberTextRunAdr.setText(String.format("%04X",Config.nRunBinAddress));

    }


     public void showDialog() {
        refreshDlg();
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextBinFile = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButtonBinOpen = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jNumberTextSavAdr = new utils.JNumberTextField();
        jNumberTextRunAdr = new utils.JNumberTextField();
        jButtonOK = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Upload file into memory");

        jLabel1.setText("File");

        jButtonBinOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/osn_open.png"))); // NOI18N
        jButtonBinOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBinOpenActionPerformed(evt);
            }
        });

        jLabel2.setText("insert from address:");

        jNumberTextSavAdr.setText("0000");
        jNumberTextSavAdr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jNumberTextSavAdrFocusLost(evt);
            }
        });

        jNumberTextRunAdr.setText("0000");
        jNumberTextRunAdr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jNumberTextRunAdrFocusLost(evt);
            }
        });

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jCheckBox1.setText("run from address:");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
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
                        .addComponent(jTextBinFile, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBinOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jNumberTextSavAdr, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(jCheckBox1)
                    .addComponent(jNumberTextRunAdr, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonOK)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextBinFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBinOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNumberTextSavAdr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNumberTextRunAdr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOK)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

     private static String getPath() {
        String retVal = "";
        if(Config.strBinFilePath.isEmpty()){
         retVal = Iq.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        }else{
         retVal=Config.strBinFilePath;  
        }
        if (retVal.contains("/")) {
            int pos = retVal.lastIndexOf("/");
            retVal = retVal.substring(0, pos + 1);
        }
        return retVal;
    }
    
    private void jButtonBinOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBinOpenActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open binary file");
        fc.resetChoosableFileFilters();
        fc.setCurrentDirectory(new File(getPath()));
        fc.setAcceptAllFileFilterUsed(true);
        int val = fc.showOpenDialog(this);
        
        if (val==JFileChooser.APPROVE_OPTION) {
            try {
                Config.strBinFilePath=fc.getSelectedFile().getCanonicalPath();
                //jTextBinFile.setText(fc.getSelectedFile().getCanonicalPath());
                Config.SaveConfig();
                refreshDlg();
            } catch (IOException ex) {
               
            }
        }
    }//GEN-LAST:event_jButtonBinOpenActionPerformed

    private void jNumberTextSavAdrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jNumberTextSavAdrFocusLost
        int nNewAdr = Integer.valueOf(jNumberTextSavAdr.getText(), 16);
        Config.nBeginBinAddress=nNewAdr;
        Config.SaveConfig();
    }//GEN-LAST:event_jNumberTextSavAdrFocusLost

    private void jNumberTextRunAdrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jNumberTextRunAdrFocusLost
       int nNewAdr = Integer.valueOf(jNumberTextRunAdr.getText(), 16);
       Config.nRunBinAddress=nNewAdr;
       Config.SaveConfig();
    }//GEN-LAST:event_jNumberTextRunAdrFocusLost

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
       if(jCheckBox1.isSelected()){
         Config.bRunBin=true;  
       }else{
         Config.bRunBin=false;  
       }
       Config.SaveConfig();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        BufferedInputStream fIn;
        try {           
                //bez hlavicky
                int nMemAdr = Config.nBeginBinAddress;
                fIn = new BufferedInputStream(new FileInputStream(Config.strBinFilePath));
                byte[] contents = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = fIn.read(contents)) != -1) {
                    for (int i = 0; i < bytesRead; i++) {
                        m.mem.writeByte(nMemAdr, contents[i]);
                        nMemAdr++;
                    }
                }
                fIn.close();
                if (Config.bRunBin) {
                    m.cpu.setRegPC(Config.nRunBinAddress);
                }
            
        } catch (Exception e) {
        }
        m.startEmulation();
        dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBinOpen;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private utils.JNumberTextField jNumberTextRunAdr;
    private utils.JNumberTextField jNumberTextSavAdr;
    private javax.swing.JTextField jTextBinFile;
    // End of variables declaration//GEN-END:variables
}
