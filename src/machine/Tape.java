/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import java.io.File;
import java.io.IOException;
import utils.CswFile;
import utils.TapeSignalProc;
import utils.WavFile;

/**
 *
 * @author admin
 */
public class Tape implements ClockTimeoutListener {
    private final int ts = 2048000; // CPU T states per second
    private Iq m;
    private TapeSignalProc tsp;
    private int[] tbuff = new int[1];

    private File LoadTape, SaveTape;
    private CswFile lcsw, scsw;
    private WavFile lwav;
    private int lrate, srate;

    private enum st {CLOSE, CSW, WAV};
    private st lst, sst;
    private boolean record;
    
    public Tape(Iq machine) {
        m = machine;
        tsp = new TapeSignalProc(256);
        lst = sst = st.CLOSE;
        record = false;
    }

    public void openLoadTape(String canonicalPath) {
        LoadTape = new File(canonicalPath);
        if (LoadTape.exists()) {
            try {
                lcsw = CswFile.openCswFile(LoadTape);
                lrate = (int) (ts / lcsw.getSampleRate());
                lst = st.CSW;
            } catch (IOException ex) {
                lst = st.CLOSE;
                try {
                    lwav = WavFile.openWavFile(LoadTape);
                    lrate = (int) (ts / lwav.getSampleRate());
                    lst = st.WAV;
                    lwav.display();
                } catch (Exception ex1) {
                    lst = st.CLOSE;
                }
            }
        }
    }

    public void openSaveTape(String canonicalPath) {
        if (sst==st.CSW) {
            try {
                scsw.close();
            } catch (IOException ex) {
            }
         }
        
        SaveTape = new File(canonicalPath);
        try {
            scsw = CswFile.openCswFile(SaveTape);
            srate = (int) (ts / scsw.getSampleRate());
            sst = st.CSW;
            scsw.setRecord(true);
        } catch (IOException ex) {
            sst = st.CLOSE;
        }
    }

    public void tapeStart() {
        m.clk.addClockTimeoutListener(this);
        m.clk.setTimeout(512);
    }

    public void tapeStop() {
        m.clk.removeClockTimeoutListener(this);
    }

    void setTapeMode(boolean rec) {
        record = rec;
    }

@Override
    public void clockTimeout() {
        if (record) {
            if (sst==st.CSW) {
                m.clk.setTimeout(srate);
                scsw.writeSample(m.getTapeOut());
            } 
            else {
                m.clk.setTimeout(512);
            }
        }
        else {
            switch (lst) {
                case CSW: {
                    m.clk.setTimeout(lrate);
                    m.setTapeIn(lcsw.readSample());
                    break;
                }
                
                case WAV: {
                    m.clk.setTimeout(lrate);
                    try {
                        lwav.readFrames(tbuff, 1);
                    } catch (Exception ex) {
                    }
                    m.setTapeIn(tsp.addSample(tbuff[0]));
                    break;
                }
                
                default: {
                    m.clk.setTimeout(512);
                }    
            }
        }
    }

    public void closeCleanup() {
        if (sst==st.CSW) {
            try {
                scsw.close();
            } catch (IOException ex) {
            }
        }
    }
}
