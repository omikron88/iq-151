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
public class Staper {
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

}
