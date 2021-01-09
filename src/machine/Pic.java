/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

/**
 *
 * @author Administrator
 */
public final class Pic {
    
    private I8085 cpu;
    
    private int icw1;
    private int icw2;
    private int icw3;
    private int icw4;
    
    private int ocw1;
    private int ocw2;
    private int ocw3;
                
    private int irr;
    private int isr;
    
    public enum init {I2,I3,I4,DONE};
    public init state;
   
    public enum iack {NONE,CALL,ADDR}
    public iack intack;
      
    public Pic() {
        Reset();
    }
    
    public PicState getPicSate() {
        PicState st = new PicState();
        
        st.icw1 = icw1;
        st.icw2 = icw2;
        st.icw3 = icw3;
        st.icw4 = icw4;
    
        st.ocw1 = ocw1;
        st.ocw2 = ocw2;
        st.ocw3 = ocw3;
                
        st.irr = irr;
        st.isr = isr;
    
        st.state = state;
        st.intack = intack;
        
        return st;
    }

    public void setPicSate(PicState st) {
        icw1 = st.icw1;
        icw2 = st.icw2;
        icw3 = st.icw3;
        icw4 = st.icw4;
    
        ocw1 = st.ocw1;
        ocw2 = st.ocw2;
        ocw3 = st.ocw3;
                
        irr = st.irr;
        isr = st.isr;
    
        state = st.state;
        intack = st.intack;
        
        checkInt();
    }

    public void Reset() {
        icw1 = 0;
        icw2 = 0;
        icw3 = 0;
        icw4 = 0;
        
        ocw1 = 0;
        ocw2 = 0;
        ocw3 = 0x02;
        
        irr = 0;
        isr = 0;
        
        state = init.DONE;
        intack = iack.NONE;
    }
    
    public void writePortA0(int value) {
        if ((value & 0x10)!=0) {  // icw1
            icw1 = value;
            ocw1 = 0;
            if ((value & 0x01)==0)
                icw4 = 0;
            ocw3 &= 0xf8; ocw3 |= 0x02;
            state = init.I2;
        } // icw1
        else { //
            if ((value & 0x08)!=0) {
                ocw3 = value;
            }
            else {
                ocw2 = value;
                doCommand(value);
            }
        } // init sequence completed
    }

    public void writePortA1(int value) {
        if (state==init.I2) { // icw2
            icw2 = value;
            if ((icw1 & 0x02)==0) { // icw3 needed
                state = init.I3;
            }
            else {
                if ((icw1 & 0x01)!=0) { // icw4 needed
                    state = init.I4;
                }
                else {
                    state = init.DONE;
                }
            }
        } // icw2
        else if (state==init.I3) { // icw3
            icw3 = value;
            if ((icw1 & 0x01)!=0) { // icw4 needed
                state = init.I4;
            }
            else {
                state = init.DONE;
            }               
        }  // icw3
        else if (state==init.I4) { // icw4
            icw4 = value;
            state = init.DONE;
        } // icw4
        else {  //init done
          ocw1 = value;
          checkInt();
        }
    }
    
    public int readPortA0() {
        if ((ocw3 & 0x04)!=0) {
            return getPoll();
        }
        else {
            if ((ocw3 & 0x03)==0x03) {
                return isr;
            }
            else
                if ((ocw3 & 0x02)==0x02) {
                    return irr;
                }
                else {
                  return 0xff;    
                }
        }
    }
    
    public int readPortA1() {
        return ocw1;
    }

    public int getIntAckCycle() {
        int rq;
        
        switch(intack) {
            case CALL:
                rq = getHighest(irr & (~ocw1));
                isr |= numToBit(rq);
                irr &= ~numToBit(rq);
                intack = iack.ADDR;
                return 0xcd; // CALL instruction
            case ADDR:
                rq = getHighest(isr);
                int a = (icw2 << 8) & 0xff00;
                cpu.setActiveInt(false);
                intack = iack.NONE;
                if ((icw4 & 0x02)!=0)
                    doCommand(0x20);
                if ((icw1 & 0x04)!=0) { // x4 mode
                    return a | (icw1 & 0xe0) | (rq<<2);    
                }
                else { // x8 mode
                    return a | (icw1 & 0xc0) | (rq<<3);                        
                }
        }
        return 0x00; // NOP instruction if faulty
    }
    
    public void assertInt(int level) {
        irr |= numToBit(level);
        checkInt();
    }
    
    public void setCPU(I8085 processor) {
        cpu = processor;
    }
    
    private int numToBit(int intnum) {
        return (1 << intnum);
    }
    
    private int getHighest(int reg) {
        for(int i=7, mask=0x80; i>-1; i--, mask>>>=1) {
            if ((reg & mask)!=0) {
                return i;
            }
        }
        return -1;
    }
    
    public int getPoll() {
        if (isr==0) {
            return 0;
        }
        else {
            return getHighest(isr) | 0x80;
        }
    }
    
    private void checkInt() {
        int rq = getHighest(irr & (~ocw1));
        int cu = getHighest(isr);
        if (rq>cu) {
            intack = iack.CALL;
            cpu.setActiveInt(true);
        }    
    }
    
    private void doCommand(int code) {
        if ((code & 0x20)!=0) { // EOI command
            int rq;
            if ((code & 0x40)!=0) { // specific EOI
                rq = ocw2 & 0x07;
            }
            else { // non specific EOI
                rq = getHighest(isr);
            }
        isr &= ~numToBit(rq);
        checkInt();
        }
    }
}