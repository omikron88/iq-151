/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

/**
 *
 * @author admin
 */
public class FloppyData {
    public byte[] dimage = null;
    public int tracksize;
    FloppyData() {
        dimage = new byte[256256];
        tracksize = 3328;
    }
}
