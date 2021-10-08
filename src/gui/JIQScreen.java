package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author admin
 */
public class JIQScreen extends javax.swing.JPanel {

    private BufferedImage image;
    
    private AffineTransform tr;
    private AffineTransformOp trOp;
    private RenderingHints rHints;
    
    /**
     * Creates new form JIQScreen
     */
    public JIQScreen() {
        initComponents();
        
        image = null;
        
        tr = AffineTransform.getScaleInstance(1.0f, 2.0f);
        
        rHints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        rHints.put(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_SPEED);
        rHints.put(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);
        rHints.put(RenderingHints.KEY_COLOR_RENDERING,
            RenderingHints.VALUE_COLOR_RENDER_SPEED);
        
        trOp = new AffineTransformOp(tr, rHints);
        
        setMinimumSize(new Dimension(560, 544));
        setMaximumSize(new Dimension(560, 544));
        setPreferredSize(new Dimension(560, 544));
       // setSize(new Dimension(640, 512));
    } // constructor

    public void setImage(BufferedImage img) {
        image = img;
    } // setImage
                
    @Override
    public void paintComponent(Graphics gc) {
        Graphics2D gc2 = (Graphics2D) gc;
        
        if (image!=null) {
            gc2.drawImage(image, trOp, 0, 0);
        }
    } // paintComponent
            
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDoubleBuffered(false);
        setMaximumSize(new java.awt.Dimension(396, 296));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 296, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
