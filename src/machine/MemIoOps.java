/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package machine;

/**
 *
 * @author jsanchez
 */
public interface MemIoOps {
    int fetchOpcode(int address);

    int peek8(int address);
    void poke8(int address, int value);
    int peek16(int address);
    void poke16(int address, int word);

    int inPort(int port);
    void outPort(int port, int value);
    
    boolean inSerial();
    void outSerial(boolean sod);
}
