/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author admin
 */
public final class Keyboard implements KeyListener {

    private int[] row;
    private boolean shift;
    private boolean ctrl;
    private boolean fa;
    private boolean fb;
    
    private Pic ic;
    private Iq machine;
    
    private final int sb0 = 0x01;
    private final int sb1 = 0x02;
    private final int sb2 = 0x04;
    private final int sb3 = 0x08;
    private final int sb4 = 0x10;
    private final int sb5 = 0x20;
    private final int sb6 = 0x40;
    private final int sb7 = 0x80;
    
    private final int rb0 = ~sb0;
    private final int rb1 = ~sb1;
    private final int rb2 = ~sb2;
    private final int rb3 = ~sb3;
    private final int rb4 = ~sb4;
    private final int rb5 = ~sb5;
    private final int rb6 = ~sb6;
    private final int rb7 = ~sb7;
    
    public Keyboard() {
        row = new int[8];
        Reset();
    }
    
    public void Reset() {
        row[0] = 0xff;
        row[1] = 0xff;
        row[2] = 0xff;
        row[3] = 0xff;
        row[4] = 0xff;
        row[5] = 0xff;
        row[6] = 0xff;
        row[7] = 0xff;
        
        shift = false;
        ctrl = false;
        fa = false;
        fb = false;
    }
    
    public void setPic(Pic pic) {
        ic = pic;
    }
    
    public void setMachine(Iq m) {
        machine = m;
    }
    
    public int readKeyboardPortA(int port) {
        int keys = 0xff;
        for(int i=0,mask=1; i<8; i++,mask<<=1) {
            if ((port & mask)==0) {
                keys &= row[i];
            }
        }
        return keys;
    }
    
    public int readKeyboardPortB(int port) {
        int keys = 0xff;
        for(int i=0,mask=1; i<8; i++,mask<<=1) {
            if ((row[i] | port)!=0xff) {
                keys &= mask ^ 0xff;
            }
        }
        return keys;    
    }

    public boolean isShift() {
        return shift;
    }
    
    public boolean isCtrl() {
        return ctrl;
    }
    
    public boolean isFA() {
        return fa;
    }
    
    public boolean isFB() {
        return fb;
    }
    
    @Override
    public void keyTyped(KeyEvent ke) {
  
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_F7:        // RES - F7
                machine.Reset(false);
                break;
            case KeyEvent.VK_F6:        // BR - F6
                ic.assertInt(5);
                break;

            case KeyEvent.VK_SHIFT:     // SH
                shift = true;
                break;
            case KeyEvent.VK_CONTROL:   // CTRL
                ctrl = true;
                break;
            case KeyEvent.VK_ALT:       // FA - Alt
                fa = true;
                break;
            case KeyEvent.VK_ALT_GRAPH: // FB - AltGr
                fb = true;
                break;
              
            case KeyEvent.VK_1:        // 1
                row[0] &= rb0;
                break;
            case KeyEvent.VK_2:        // 2
                row[0] &= rb1;
                break;
            case KeyEvent.VK_3:        // 3
                row[0] &= rb2;
                break;
            case KeyEvent.VK_4:        // 4
                row[0] &= rb3;
                break;
            case KeyEvent.VK_5:        // 5
                row[0] &= rb4;
                break;
            case KeyEvent.VK_6:        // 6
                row[0] &= rb5;
                break;
            case KeyEvent.VK_7:        // 7
                row[0] &= rb6;
                break;
            case KeyEvent.VK_8:        // 8
                row[0] &= rb7;
                break;
            case KeyEvent.VK_NUMPAD1:        // 1
                row[0] &= rb0;
                break;
            case KeyEvent.VK_NUMPAD2:        // 2
                row[0] &= rb1;
                break;
            case KeyEvent.VK_NUMPAD3:        // 3
                row[0] &= rb2;
                break;
            case KeyEvent.VK_NUMPAD4:        // 4
                row[0] &= rb3;
                break;    
            case KeyEvent.VK_NUMPAD5:        // 5
                row[0] &= rb4;
                break;
             case KeyEvent.VK_NUMPAD6:        // 6
                row[0] &= rb5;
                break;
            case KeyEvent.VK_NUMPAD7:        // 7
                row[0] &= rb6;
                break;
            case KeyEvent.VK_NUMPAD8:        // 8
                row[0] &= rb7;
                break;    

            case KeyEvent.VK_Q:         // Q
                row[1] &= rb0;
                break;
            case KeyEvent.VK_W:         // W
                row[1] &= rb1;
                break;
            case KeyEvent.VK_E:         // E
                row[1] &= rb2;
                break;
            case KeyEvent.VK_R:         // R
                row[1] &= rb3;
                break;
            case KeyEvent.VK_T:         // T
                row[1] &= rb4;
                break;
            case KeyEvent.VK_Y:         // Y
                row[1] &= rb5;
                break;
            case KeyEvent.VK_U:         // U
                row[1] &= rb6;
                break;
            case KeyEvent.VK_I:         // I
                row[1] &= rb7;
                break;

            case KeyEvent.VK_A:         // A
                row[2] &= rb0;
                break;
            case KeyEvent.VK_S:         // S
                row[2] &= rb1;
                break;
            case KeyEvent.VK_D:         // D
                row[2] &= rb2;
                break;
            case KeyEvent.VK_F:         // F
                row[2] &= rb3;
                break;
            case KeyEvent.VK_G:         // G
                row[2] &= rb4;
                break;
            case KeyEvent.VK_H:         // H
                row[2] &= rb5;
                break;
            case KeyEvent.VK_J:         // J
                row[2] &= rb6;
                break;
            case KeyEvent.VK_K:         // K
                row[2] &= rb7;
                break;

            case KeyEvent.VK_Z:         // Z
                row[3] &= rb0;
                break;
            case KeyEvent.VK_X:         // X
                row[3] &= rb1;
                break;
            case KeyEvent.VK_C:         // C
                row[3] &= rb2;
                break;
            case KeyEvent.VK_V:         // V
                row[3] &= rb3;
                break;
            case KeyEvent.VK_B:         // B
                row[3] &= rb4;
                break;
            case KeyEvent.VK_N:         // N
                row[3] &= rb5;
                break;
            case KeyEvent.VK_M:         // M
                row[3] &= rb6;
                break;
            case KeyEvent.VK_COMMA:     // ,
                row[3] &= rb7;
                break;
            case KeyEvent.VK_9:         // 9
                row[4] &= rb0;
                break;
            case KeyEvent.VK_NUMPAD9:         // 9
                row[4] &= rb0;
                break;
            case KeyEvent.VK_O:         // O
                row[4] &= rb1;
                break;
            case KeyEvent.VK_L:         // L
                row[4] &= rb2;
                break;
            case KeyEvent.VK_PERIOD:    // .
                row[4] &= rb3;
                break;
            case KeyEvent.VK_ENTER:     // CR
                row[4] &= rb4;
                break;
            case KeyEvent.VK_DELETE:    // DC - Del
                row[4] &= rb5;
                break;
            case KeyEvent.VK_INSERT:    // IC - Ins
                row[4] &= rb6;
                break;
            case KeyEvent.VK_F1:        // F1
                row[4] &= rb7;
                break;
                
            case KeyEvent.VK_0:         // 0
                row[5] &= rb0;
                break;
            case KeyEvent.VK_NUMPAD0:         // 0
                row[5] &= rb0;
                break;
            case KeyEvent.VK_P:         // P
                row[5] &= rb1;
                break;
            case KeyEvent.VK_SEMICOLON: // ;
                row[5] &= rb2;
                break;
            case KeyEvent.VK_SLASH:     // /
                row[5] &= rb3;
                break;
            case KeyEvent.VK_BACK_SLASH:// \
                row[5] &= rb4;
                break;
            case KeyEvent.VK_PAGE_DOWN: // DL - PgDown
                row[5] &= rb5;
                break;
            case KeyEvent.VK_LEFT:      // LEFT
                row[5] &= rb6;
                break;
            case KeyEvent.VK_F2:        // F2
                row[5] &= rb7;
                break;

            case KeyEvent.VK_EQUALS:    // ^ - =
                row[6] &= rb0;
                break;
            case KeyEvent.VK_OPEN_BRACKET: // [
                row[6] &= rb1;
                break;
            case KeyEvent.VK_CLOSE_BRACKET: // ]
                row[6] &= rb2;
                break;
            case KeyEvent.VK_DOWN:      // DOWN
                row[6] &= rb3;
                break;
            case KeyEvent.VK_F5:         // F5
                row[6] &= rb4;
                break;
            case KeyEvent.VK_PAGE_UP:   // IL - PgUp
                row[6] &= rb5;
                break;
            case KeyEvent.VK_HOME:      // Home
                row[6] &= rb6;
                break;
            case KeyEvent.VK_F4:        // F4
                row[6] &= rb7;
                break;

            case KeyEvent.VK_MINUS:     // -
                row[7] &= rb0;
                break;
            case KeyEvent.VK_BACK_QUOTE:// @ - `
                row[7] &= rb1;
                break;
            case KeyEvent.VK_QUOTE:     // : - '
                row[7] &= rb2;
                break;
            case KeyEvent.VK_SPACE:     // SPACE
                row[7] &= rb3;
                break;
            case KeyEvent.VK_UNDERSCORE:// _
                row[7] &= rb4;
                break;
            case KeyEvent.VK_UP:        // UP
                row[7] &= rb5;
                break;
            case KeyEvent.VK_RIGHT:     // RIGHT
                row[7] &= rb6;
                break;
            case KeyEvent.VK_F3:        // F3
                row[7] &= rb7;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        switch(ke.getKeyCode()) {

            case KeyEvent.VK_SHIFT:     // SH
                shift = false;
                break;
            case KeyEvent.VK_CONTROL:   // CTRL
                ctrl = false;
                break;
            case KeyEvent.VK_ALT:       // FA - Alt
                fa = false;
                break;
            case KeyEvent.VK_ALT_GRAPH: // FB - AltGr
                fb = false;
                break;
/*
            case KeyEvent.VK_F12: 
                if(machine.cpu.bStartWrite){
                 machine.cpu.bStartWrite=false;
                }else{
                 machine.cpu.bStartWrite=true;
                }
                break;              
*/  
            case KeyEvent.VK_1:        // 1
                row[0] |= sb0;
                break;
            case KeyEvent.VK_2:        // 2
                row[0] |= sb1;
                break;
            case KeyEvent.VK_3:        // 3
                row[0] |= sb2;
                break;
            case KeyEvent.VK_4:        // 4
                row[0] |= sb3;
                break;
            case KeyEvent.VK_5:        // 5
                row[0] |= sb4;
                break;
            case KeyEvent.VK_6:        // 6
                row[0] |= sb5;
                break;
            case KeyEvent.VK_7:        // 7
                row[0] |= sb6;
                break;
            case KeyEvent.VK_8:        // 8
                row[0] |= sb7;
                break;
            case KeyEvent.VK_NUMPAD1:        // 1
                row[0] |= sb0;
                break;
            case KeyEvent.VK_NUMPAD2:        // 2
                row[0] |= sb1;
                break;
            case KeyEvent.VK_NUMPAD3:        // 3
                row[0] |= sb2;
                break;
            case KeyEvent.VK_NUMPAD4:        // 4
                row[0] |= sb3;
                break;
            case KeyEvent.VK_NUMPAD5:        // 5
                row[0] |= sb4;
                break;
            case KeyEvent.VK_NUMPAD6:        // 6
                row[0] |= sb5;
                break;
            case KeyEvent.VK_NUMPAD7:        // 7
                row[0] |= sb6;
                break;
            case KeyEvent.VK_NUMPAD8:        // 8
                row[0] |= sb7;
                break;

            case KeyEvent.VK_Q:         // Q
                row[1] |= sb0;
                break;
            case KeyEvent.VK_W:         // W
                row[1] |= sb1;
                break;
            case KeyEvent.VK_E:         // E
                row[1] |= sb2;
                break;
            case KeyEvent.VK_R:         // R
                row[1] |= sb3;
                break;
            case KeyEvent.VK_T:         // T
                row[1] |= sb4;
                break;
            case KeyEvent.VK_Y:         // Y
                row[1] |= sb5;
                break;
            case KeyEvent.VK_U:         // U
                row[1] |= sb6;
                break;
            case KeyEvent.VK_I:         // I
                row[1] |= sb7;
                break;

            case KeyEvent.VK_A:         // A
                row[2] |= sb0;
                break;
            case KeyEvent.VK_S:         // S
                row[2] |= sb1;
                break;
            case KeyEvent.VK_D:         // D
                row[2] |= sb2;
                break;
            case KeyEvent.VK_F:         // F
                row[2] |= sb3;
                break;
            case KeyEvent.VK_G:         // G
                row[2] |= sb4;
                break;
            case KeyEvent.VK_H:         // H
                row[2] |= sb5;
                break;
            case KeyEvent.VK_J:         // J
                row[2] |= sb6;
                break;
            case KeyEvent.VK_K:         // K
                row[2] |= sb7;
                break;

            case KeyEvent.VK_Z:         // Z
                row[3] |= sb0;
                break;
            case KeyEvent.VK_X:         // X
                row[3] |= sb1;
                break;
            case KeyEvent.VK_C:         // C
                row[3] |= sb2;
                break;
            case KeyEvent.VK_V:         // V
                row[3] |= sb3;
                break;
            case KeyEvent.VK_B:         // B
                row[3] |= sb4;
                break;
            case KeyEvent.VK_N:         // N
                row[3] |= sb5;
                break;
            case KeyEvent.VK_M:         // M
                row[3] |= sb6;
                break;
            case KeyEvent.VK_COMMA:     // ,
                row[3] |= sb7;
                break;

            case KeyEvent.VK_9:         // 9
                row[4] |= sb0;
                break;
            case KeyEvent.VK_NUMPAD9:         // 9
                row[4] |= sb0;
                break;
            case KeyEvent.VK_O:         // O
                row[4] |= sb1;
                break;
            case KeyEvent.VK_L:         // L
                row[4] |= sb2;
                break;
            case KeyEvent.VK_PERIOD:    // .
                row[4] |= sb3;
                break;
            case KeyEvent.VK_ENTER:     // CR
                row[4] |= sb4;
                break;
            case KeyEvent.VK_DELETE:    // DC - Del
                row[4] |= sb5;
                break;
            case KeyEvent.VK_INSERT:    // IC - Ins
                row[4] |= sb6;
                break;
            case KeyEvent.VK_F1:        // F1
                row[4] |= sb7;
                break;
                
            case KeyEvent.VK_0:         // 0
                row[5] |= sb0;
                break;
            case KeyEvent.VK_NUMPAD0:         // 0
                row[5] |= sb0;
                break;
            case KeyEvent.VK_P:         // P
                row[5] |= sb1;
                break;
            case KeyEvent.VK_SEMICOLON: // ;
                row[5] |= sb2;
                break;
            case KeyEvent.VK_SLASH:     // /
                row[5] |= sb3;
                break;
            case KeyEvent.VK_BACK_SLASH:// \
                row[5] |= sb4;
                break;
            case KeyEvent.VK_PAGE_DOWN: // DL - PgDown
                row[5] |= sb5;
                break;
            case KeyEvent.VK_LEFT:      // LEFT
                row[5] |= sb6;
                break;
            case KeyEvent.VK_F2:        // F2
                row[5] |= sb7;
                break;

            case KeyEvent.VK_EQUALS:    // ^ - =
                row[6] |= sb0;
                break;
            case KeyEvent.VK_OPEN_BRACKET: // [
                row[6] |= sb1;
                break;
            case KeyEvent.VK_CLOSE_BRACKET: // ]
                row[6] |= sb2;
                break;
            case KeyEvent.VK_DOWN:      // DOWN
                row[6] |= sb3;
                break;
            case KeyEvent.VK_F5:         // F5
                row[6] |= sb4;
                break;
            case KeyEvent.VK_PAGE_UP:   // IL - PgUp
                row[6] |= sb5;
                break;
            case KeyEvent.VK_HOME:      // Home
                row[6] |= sb6;
                break;
            case KeyEvent.VK_F4:        // F4
                row[6] |= sb7;
                break;

            case KeyEvent.VK_MINUS:     // -
                row[7] |= sb0;
                break;
            case KeyEvent.VK_BACK_QUOTE:// @ - `
                row[7] |= sb1;
                break;
            case KeyEvent.VK_QUOTE:     // : - '
                row[7] |= sb2;
                break;
            case KeyEvent.VK_SPACE:     // SPACE
                row[7] |= sb3;
                break;
            case KeyEvent.VK_UNDERSCORE:// _
                row[7] |= sb4;
                break;
            case KeyEvent.VK_UP:        // UP
                row[7] |= sb5;
                break;
            case KeyEvent.VK_RIGHT:     // RIGHT
                row[7] |= sb6;
                break;
            case KeyEvent.VK_F3:     // F3
                row[7] |= sb7;
                break;

        }
    }
    
}
