/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

/**
 *
 * @author Administrator
 */
public interface Pio8255Notify {
    
    void OnCpuWriteA();
    void OnCpuWriteB();
    void OnCpuWriteC();
    void OnCpuWriteCL();
    void OnCpuWriteCH();
    
    void OnCpuWriteCWR(int value);
    
    void OnCpuReadA();
    void OnCpuReadB();
    void OnCpuReadC();
    void OnCpuReadCL();
    void OnCpuReadCH();
    
}
