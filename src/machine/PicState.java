/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

/**
 *
 * @author Administrator
 */
public class PicState {
    public int icw1;
    public int icw2;
    public int icw3;
    public int icw4;
    
    public int ocw1;
    public int ocw2;
    public int ocw3;
                
    public int irr;
    public int isr;
    
    public Pic.init state;   
    public Pic.iack intack;
}
