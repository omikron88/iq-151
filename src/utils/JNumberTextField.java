/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.event.KeyEvent;
import javax.swing.JTextField;

public class JNumberTextField extends JTextField {
    private static final long serialVersionUID = 1L;

    @Override
    public void processKeyEvent(KeyEvent ev) {
        boolean bKeyPressed=false;
        int keyCode = ev.getKeyCode();
        char keyChar = Character.toUpperCase(ev.getKeyChar());
        if ((keyCode == KeyEvent.VK_LEFT) || (keyCode == KeyEvent.VK_RIGHT)) {
            super.processKeyEvent(ev);
            bKeyPressed=true;
        }
        if ((keyCode == KeyEvent.VK_ENTER)|| (keyCode == KeyEvent.VK_ESCAPE)) {
           transferFocus();
           super.processKeyEvent(ev);
            bKeyPressed=true; 
        }
        if ((Character.isDigit(keyChar))||(keyChar=='A')||(keyChar=='B')||(keyChar=='C')||(keyChar=='D')||(keyChar=='E')||(keyChar=='F')) {
            super.processKeyEvent(ev);
            bKeyPressed=true;
        }
        if(bKeyPressed){
        String text = getText();
        int caretPosition = getCaretPosition();
        String strNewText=text.substring(0,4);
        setText(strNewText);
        setCaretPosition(caretPosition);
        }
      
        ev.consume();
        return;
    }

    /**
     * As the user is not even able to enter a dot ("."), only integers (whole numbers) may be entered.
     */
    public Long getNumber() {
        Long result = null;
        String text = getText();
        if (text != null && !"".equals(text)) {
            result = Long.valueOf(text);
        }
        return result;
    }
}
