/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import gui.JIQ151;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class Staper implements Pio8255Notify {
    private final Pio8255 pio;
    private String path;
    
    public Staper() {
        path = getMyPath()+"sd";
        if (Files.isDirectory(Paths.get(path))==false) {
            try {
                Files.createDirectory(Paths.get(path));
            } catch (IOException ex) {
                Logger.getLogger(Staper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        pio = new Pio8255(this);        
    }
    
    private String getMyPath() {
        String retVal = "";
        retVal = JIQ151.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (retVal.contains("/")) {
            int pos = retVal.lastIndexOf("/");
            retVal = retVal.substring(0, pos + 1);
        }
        return retVal;
    }

    @Override
    public void OnCpuWriteA() {
    }

    @Override
    public void OnCpuWriteB() {
    }

    @Override
    public void OnCpuWriteC() {
    }

    @Override
    public void OnCpuWriteCL() {
    }

    @Override
    public void OnCpuWriteCH() {
    }

    @Override
    public void OnCpuWriteCWR(int value) {
    }

    @Override
    public void OnCpuReadA() {
    }

    @Override
    public void OnCpuReadB() {
    }

    @Override
    public void OnCpuReadC() {
    }

    @Override
    public void OnCpuReadCL() {
    }

    @Override
    public void OnCpuReadCH() {
    }

}
