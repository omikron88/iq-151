/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import java.util.TimerTask;

public class IqTimer extends TimerTask {

    private Iq iq;
    private long now, diff;

    public IqTimer(Iq iq) {
        this.iq = iq;
    }

    @Override
    public synchronized void run() {

        now = System.currentTimeMillis();
        diff = now - scheduledExecutionTime();
        if (diff < 51) {
            iq.ms20();
        }
    }
}
