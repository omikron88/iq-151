package machine;

import java.util.Arrays;

public class I8085 {

    boolean bStartWrite=false;
    public final Clock clock;
    private final MemIoOps MemIoImpl;
    private final NotifyOps NotifyImpl;
    public boolean bMemBP = false;
    private int opCode;

    private boolean execDone;

    private static final int C_MASK = 0x01;
    private static final int V_MASK = 0x02;
    private static final int P_MASK = 0x04;
    private static final int H_MASK = 0x10;
    private static final int U_MASK = 0x20;
    private static final int Z_MASK = 0x40;
    private static final int S_MASK = 0x80;

    private int regA, regB, regC, regD, regE, regH, regL;

    private int Flags;

    private boolean carryFlag;
    private boolean UFlag;
    private boolean VFlag;

    private int regSim;

    private int regPC;
    private int regSP;

    private boolean pendingEI = false;
    private boolean activeTRAP = false;
    private boolean activeINT = false;
    private boolean intack = false;

    private boolean halted = false;

    private int memptr;

    private static final int fl[] = new int[256];

    static {
        boolean evenBits;

        for (int idx = 0; idx < 256; idx++) {
            if (idx > 0x7f) {
                fl[idx] |= S_MASK;
            }

            evenBits = true;
            for (int mask = 0x01; mask < 0x100; mask <<= 1) {
                if ((idx & mask) != 0) {
                    evenBits = !evenBits;
                }
            }

            if (evenBits) {
                fl[idx] |= P_MASK;
            }
        }

        fl[0] |= Z_MASK;
    }

    private boolean breakpointAt[] = new boolean[65536];

    public I8085(Clock clock, MemIoOps memory, NotifyOps notify) {
        this.clock = clock;
        MemIoImpl = memory;
        NotifyImpl = notify;
        execDone = false;
        Arrays.fill(breakpointAt, false);
        reset();
    }

    public final boolean isPendingEI() {
        return pendingEI;
    }

    public final void setPendingEI(boolean state) {
        pendingEI = state;
    }

    public final boolean isActiveInt() {
        return activeINT;
    }

    public final boolean isIntAck() {
        return intack;
    }

    public final void setActiveInt(boolean state) {
        activeINT = state;
    }

    public final int getRegA() {
        return regA;
    }

    public final void setRegA(int value) {
        regA = value & 0xff;
    }

    public final int getRegB() {
        return regB;
    }

    public final void setRegB(int value) {
        regB = value & 0xff;
    }

    public final int getRegC() {
        return regC;
    }

    public final void setRegC(int value) {
        regC = value & 0xff;
    }

    public final int getRegD() {
        return regD;
    }

    public final void setRegD(int value) {
        regD = value & 0xff;
    }

    public final int getRegE() {
        return regE;
    }

    public final void setRegE(int value) {
        regE = value & 0xff;
    }

    public final int getRegH() {
        return regH;
    }

    public final void setRegH(int value) {
        regH = value & 0xff;
    }

    public final int getRegL() {
        return regL;
    }

    public final void setRegL(int value) {
        regL = value & 0xff;
    }

    public final int getRegAF() {
        return (regA << 8) | (carryFlag ? Flags | C_MASK : Flags);
    }

    public final void setRegAF(int word) {
        regA = (word >>> 8) & 0xff;

        Flags = word & 0xfe;
        carryFlag = (word & C_MASK) != 0;
    }

    public final int getRegBC() {
        return (regB << 8) | regC;
    }

    public final void setRegBC(int word) {
        regB = (word >>> 8) & 0xff;
        regC = word & 0xff;
    }

    private void incRegBC() {
        int reg16 = getRegBC();
        reg16 = (++reg16) & 0xffff;
        UFlag = (reg16 == 0x8000);
        setRegBC(reg16);
    }

    private void decRegBC() {
        int reg16 = getRegBC();
        reg16 = (--reg16) & 0xffff;
        UFlag = (reg16 == 0x7fff);
        setRegBC(reg16);
    }

    public final int getRegDE() {
        return (regD << 8) | regE;
    }

    public final void setRegDE(int word) {
        regD = (word >>> 8) & 0xff;
        regE = word & 0xff;
    }

    private void incRegDE() {
        int reg16 = getRegDE();
        reg16 = (++reg16) & 0xffff;
        UFlag = (reg16 == 0x8000);
        setRegDE(reg16);
    }

    private void decRegDE() {
        int reg16 = getRegDE();
        reg16 = (--reg16) & 0xffff;
        UFlag = (reg16 == 0x7fff);
        setRegDE(reg16);
    }

    public final int getRegHL() {
        return (regH << 8) | regL;
    }

    public final void setRegHL(int word) {
        regH = (word >>> 8) & 0xff;
        regL = word & 0xff;
    }

    private void incRegHL() {
        int reg16 = getRegHL();
        reg16 = (++reg16) & 0xffff;
        UFlag = (reg16 == 0x8000);
        setRegHL(reg16);
    }

    private void decRegHL() {
        int reg16 = getRegHL();
        reg16 = (--reg16) & 0xffff;
        UFlag = (reg16 == 0x7fff);
        setRegHL(reg16);
    }

    public final int getRegPC() {
        return regPC;
    }

    public final void setRegPC(int address) {
        regPC = address & 0xffff;
    }

    public final int getRegSP() {
        return regSP & 0xffff;
    }

    public final void setRegSP(int word) {
        regSP = word & 0xffff;
    }

    private void incRegSP() {
        int reg16 = getRegSP();
        reg16 = (++reg16) & 0xffff;
        UFlag = (reg16 == 0x8000);
        setRegSP(reg16);
    }

    private void decRegSP() {
        int reg16 = getRegSP();
        reg16 = (--reg16) & 0xffff;
        UFlag = (reg16 == 0x7fff);
        setRegSP(reg16);
    }

    public final int getRegSim() {
        return regSim & 0xff;
    }

    public final void setRegSim(int value) {
        regSim = value & 0xff;
    }

    public final int getMemPtr() {
        return memptr & 0xffff;
    }

    public final void setMemPtr(int word) {
        memptr = word & 0xffff;
    }

    public final boolean isCarryFlag() {
        return carryFlag;
    }

    public final void setCarryFlag(boolean state) {
        carryFlag = state;
    }

    public final boolean isVFlag() {
        return VFlag;
    }

    public final void setVFlag(boolean state) {
        VFlag = state;
    }

    public final boolean isPFlag() {
        return (Flags & P_MASK) != 0;
    }

    public final void setPFlag(boolean state) {
        if (state) {
            Flags |= P_MASK;
        } else {
            Flags &= ~P_MASK;
        }
    }

    public final boolean isHFlag() {
        return (Flags & H_MASK) != 0;
    }

    public final void setHFlag(boolean state) {
        if (state) {
            Flags |= H_MASK;
        } else {
            Flags &= ~H_MASK;
        }
    }

    public final boolean isUFlag() {
        return UFlag;
    }

    public final void setUFlag(boolean state) {
        UFlag = state;
    }

    public final boolean isZFlag() {
        return (Flags & Z_MASK) != 0;
    }

    public final void setZFlag(boolean state) {
        if (state) {
            Flags |= Z_MASK;
        } else {
            Flags &= ~Z_MASK;
        }
    }

    public final boolean isSFlag() {
        return (Flags & S_MASK) != 0;
    }

    public final void setSFlag(boolean state) {
        if (state) {
            Flags |= S_MASK;
        } else {
            Flags &= ~S_MASK;
        }
    }

    public final int getFlags() {
        int flg = Flags;

        if (carryFlag) {
            flg |= C_MASK;
        }

        if (UFlag) {
            flg |= U_MASK;
        }

        if (VFlag) {
            flg |= V_MASK;
        }

        return flg;
    }

    public final void setFlags(int regF) {
        Flags = regF & ~(C_MASK | U_MASK | V_MASK);
        carryFlag = (regF & C_MASK) != 0;
        UFlag = (regF & U_MASK) != 0;
        VFlag = (regF & V_MASK) != 0;
    }

    public final boolean isHalted() {
        return halted;
    }

    public void setHalted(boolean state) {
        halted = state;
    }

    public final void reset() {
        regPC = 0;
        regSP = 0xffff;
        regSim = 0;
        pendingEI = false;
        halted = false;
        activeINT = activeTRAP = false;
    }

    private int inc8(int oper8) {
        oper8 = (oper8 + 1) & 0xff;

        Flags = fl[oper8];

        if ((oper8 & 0x0f) == 0) {
            Flags |= H_MASK;
        }

        VFlag = (oper8 == 0x80);

        return oper8;
    }

    private int dec8(int oper8) {
        oper8 = (oper8 - 1) & 0xff;

        Flags = fl[oper8];

        if ((oper8 & 0x0f) == 0x0f) {
            Flags |= H_MASK;
        }

        VFlag = (oper8 == 0x7f);

        return oper8;
    }

    private void add(int oper8) {
        int res = regA + oper8;

        carryFlag = res > 0xff;
        res &= 0xff;
        Flags = fl[res];

        if ((res & 0x0f) < (regA & 0x0f)) {
            Flags |= H_MASK;
        }

        VFlag = (((regA ^ ~oper8) & (regA ^ res)) > 0x7f);

        regA = res;
    }

    private void adc(int oper8) {
        int res = regA + oper8;

        if (carryFlag) {
            res++;
        }

        carryFlag = res > 0xff;
        res &= 0xff;
        Flags = fl[res];

        if (((regA ^ oper8 ^ res) & 0x10) != 0) {
            Flags |= H_MASK;
        }

        VFlag = (((regA ^ ~oper8) & (regA ^ res)) > 0x7f);

        regA = res;
    }

    private int add16(int reg16, int oper16) {
        oper16 += reg16;

        carryFlag = oper16 > 0xffff;
        VFlag = (oper16 > 0x7fff);

        oper16 &= 0xffff;

        memptr = reg16 + 1;
        return oper16;
    }

    private void sub(int oper8) {
        int res = regA - oper8;

        carryFlag = res < 0;

        res &= 0xff;
        Flags = fl[res];

        if ((res & 0x0f) > (regA & 0x0f)) {
            Flags |= H_MASK;
        }

        VFlag = (((regA ^ oper8) & (regA ^ res)) > 0x7f);

        regA = res;
    }

    private void sbc(int oper8) {
        int res = regA - oper8;

        if (carryFlag) {
            --res;
        }

        carryFlag = res < 0;
        res &= 0xff;
        Flags = fl[res];

        if (((regA ^ oper8 ^ res) & 0x10) != 0) {
            Flags |= H_MASK;
        }

        VFlag = (((regA ^ oper8) & (regA ^ res)) > 0x7f);

        regA = res;
    }

    private void sub16(int reg16) {
        int regHL = getRegHL();
        memptr = regHL + 1;

        int res = regHL - reg16;

        carryFlag = res < 0;
        res &= 0xffff;
        setRegHL(res);

        Flags = fl[regH];
        if (res != 0) {
            Flags &= ~Z_MASK;
        }
    }

    private void and(int oper8) {
        regA &= oper8;
        carryFlag = false;
        Flags = fl[regA] & ~H_MASK;
    }

    public final void xor(int oper8) {
        regA = (regA ^ oper8) & 0xff;
        carryFlag = false;
        Flags = fl[regA] & ~H_MASK;
    }

    private void or(int oper8) {
        regA = (regA | oper8) & 0xff;
        carryFlag = false;
        Flags = fl[regA] & ~H_MASK;
    }

    public final void cp(int oper8) {
        int res = regA - (oper8 & 0xff);

        carryFlag = res < 0;
        res &= 0xff;

        Flags = fl[res];

        if ((res & 0x0f) > (regA & 0x0f)) {
            Flags |= H_MASK;
        }

        VFlag = (((regA ^ oper8) & (regA ^ res)) > 0x7f);
    }

    private void daa() {
        int suma = 0;

        if ((Flags & H_MASK) != 0 || (regA & 0x0f) > 0x09) {
            suma = 6;
        }

        if (carryFlag || (regA > 0x99)) {
            suma |= 0x60;
        }

        if (regA > 0x99) {
            carryFlag = true;
        }

        add(suma);
    }

    private int pop() {
        int word = MemIoImpl.peek16(regSP);
        regSP = (regSP + 2) & 0xffff;
        return word;
    }

    private void push(int word) {
        regSP = (regSP - 1) & 0xffff;
        MemIoImpl.poke8(regSP, word >>> 8);
        regSP = (regSP - 1) & 0xffff;
        MemIoImpl.poke8(regSP, word);
    }

    public final boolean isBreakpoint(int address) {
        return breakpointAt[address & 0xffff];
    }

    public final void setBreakpoint(int address, boolean state) {
        breakpointAt[address & 0xffff] = state;
    }

    public void resetBreakpoints() {
        Arrays.fill(breakpointAt, false);
    }

    private boolean isEI() {
        return (((regSim & 0x08) != 0) && !pendingEI);
    }

    public final void doTrap() {
        activeTRAP = true;
    }

    public final void doInt75() {
        if (isEI()) {
            if ((regSim & 0x04) == 0) {
                regSim |= 0x40;
            }
        }
    }

    public final void doInt65() {
        if (isEI()) {
            if ((regSim & 0x02) == 0) {
                regSim |= 0x20;
            }
        }
    }

    public final void doInt55() {
        if (isEI()) {
            if ((regSim & 0x01) == 0) {
                regSim |= 0x10;
            }
        }
    }

    private void trap() {
        clock.addTstates(1);
        if (halted) {
            halted = false;
            regPC = (regPC + 1) & 0xffff;
        }
        regSim &= ~0x08;
        push(regPC);
        regPC = memptr = 0x0024;

    }

    private void int75() {
        if (halted) {
            halted = false;
            regPC = (regPC + 1) & 0xffff;
        }

        clock.addTstates(7);

        regSim &= ~0x48;
        push(regPC);
        regPC = memptr = 0x003C;
    }

    private void int65() {
        if (halted) {
            halted = false;
            regPC = (regPC + 1) & 0xffff;
        }

        clock.addTstates(7);

        regSim &= ~0x28;
        push(regPC);
        regPC = memptr = 0x0034;
    }

    private void int55() {
        if (halted) {
            halted = false;
            regPC = (regPC + 1) & 0xffff;
        }

        clock.addTstates(7);

        regSim &= ~0x18;
        push(regPC);
        regPC = memptr = 0x002C;
    }

    private void intr() {
        if (halted) {
            halted = false;
            regPC = (regPC + 1) & 0xffff;
        }

        clock.addTstates(7);

        regSim &= ~0x08;
        intack = true;
    }

    public final void executeOne() {

        if (activeTRAP) {
            activeTRAP = false;
            trap();
            return;
        }

        if (isEI() && ((regSim & 0x40) != 0)) {
            int75();
            return;
        }

        if (isEI() && ((regSim & 0x20) != 0)) {
            int65();
            return;
        }

        if (isEI() && ((regSim & 0x10) != 0)) {
            int55();
            return;
        }

        if (isEI() && activeINT) {
            intr();
        }

        if (breakpointAt[regPC] && !intack) {
            opCode = NotifyImpl.atAddress(regPC, opCode);
        }

        opCode = MemIoImpl.fetchOpcode(regPC);

        if (!intack) {
            regPC = (regPC + 1) & 0xffff;
        }

        decodeOpcode(opCode);

        if (pendingEI && opCode != 0xFB) {
            pendingEI = false;
        }

        if (execDone) {
            NotifyImpl.execDone();
        }

        intack = false;
    }

    public final void execute(int statesLimit) {
        bMemBP = false;
        int nStartTStates = clock.getTstates();
        while (clock.getTstates() < statesLimit) {
            //generovani pravidelnych zvukovych samplu v bufferu            
            if (((Iq) NotifyImpl).audio.isEnabled()) {
                Sound.nDecrementSampleStates -= (clock.getTstates() - nStartTStates);
                nStartTStates = clock.getTstates();
                if (Sound.nDecrementSampleStates <= 0) {
                    Sound.nDecrementSampleStates += Sound.nOneSampleStates;
                    ((Iq) NotifyImpl).audio.fillBuffer.putToBuffer();
                }
            }
            
            if (activeTRAP) {
                activeTRAP = false;
                trap();
                continue;
            }

            if (isEI() && ((regSim & 0x40) != 0)) {
                int75();
                continue;
            }

            if (isEI() && ((regSim & 0x20) != 0)) {
                int65();
                continue;
            }

            if (isEI() && ((regSim & 0x10) != 0)) {
                int55();
                continue;
            }

            if (isEI() && activeINT) {
                intr();
            }

            if (breakpointAt[regPC]) {
                if ((utils.Config.sdrom) && (utils.Config.sdromautorun) && (regPC == ((Iq) NotifyImpl).nAutoRunBreakAddress) && (((Iq) NotifyImpl).bAutoRunAfterReset)) {
                    //spusteni SDROM Autorun
                    setBreakpoint(regPC, false);
                    ((Iq) NotifyImpl).bAutoRunAfterReset = false;     
                    MemIoImpl.poke8(3, (byte)85);
                    push(0);
                    setRegPC(0xF3BA); //L v monitoru                   
                } else {
                    //klasicky Breakpoint spusti Debugger
                    opCode = NotifyImpl.atAddress(regPC, opCode);
                    Iq m = (Iq) NotifyImpl;
                    m.stopEmulation();

                    m.getDebugger().showDialog();
                    break;
                }
            }
            /*
            if (regPC == 0xc800) {
                //bStartWrite = true;
            }
            if (bStartWrite) {
                String strAsmfile = utils.Config.getMyPath() + "ASMLog.txt";
                try {
                    Z80Dis disassembler = new Z80Dis();
                    Z80Dis.Opcodes = new int[65536];
                    for (int i = getRegPC(); i < getRegPC() + 5; i++) {
                        Z80Dis.Opcodes[i] = (0xff) & (byte) MemIoImpl.peek8(i);
                    }
                    byte OpcodeLen = disassembler.OpcodeLen(getRegPC());
                    PrintWriter f = new PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(strAsmfile, true)));
                    f.println(String.format("%04X\t%s", getRegPC(), disassembler.Disassemble(getRegPC())));
                    f.close();
                } catch (FileNotFoundException ex) {
                }
            }
            */
            
            opCode = MemIoImpl.fetchOpcode(regPC);

            if (!intack) {
                regPC = (regPC + 1) & 0xffff;
            }

            decodeOpcode(opCode);
            //pokud se objevil memory breakpoint tak vyskoc
            if (bMemBP) {
                break;
            }

            if (pendingEI && opCode != 0xFB) {
                pendingEI = false;
            }

            if (execDone) {
                NotifyImpl.execDone();
            }

            intack = false;

        } // while
    }

    private void decodeOpcode(int opCode) {
        int work8, work16;
        byte offset;

        switch (opCode) {
//            case 0x00:       /* NOP */
//                break;
            case 0x01:
                /* LXI B */
                setRegBC(MemIoImpl.peek16(regPC));
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0x02:
                /* STAX B */
                MemIoImpl.poke8(getRegBC(), regA);
                memptr = (regA << 8) | ((regC + 1) & 0xff);
                break;
            case 0x03:
                /* INX B */
                incRegBC();
                break;
            case 0x04:
                /* INR B */
                regB = inc8(regB);
                break;
            case 0x05:
                /* DCR B */
                regB = dec8(regB);
                break;
            case 0x06:
                /* MVI B */
                regB = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            case 0x07:
                /* RLC */
                carryFlag = (regA > 0x7f);
                regA = (regA << 1) & 0xff;
                if (carryFlag) {
                    regA |= C_MASK;
                }
                break;
            case 0x08:
                /* DSUB */
                sub16(getRegBC());
                break;
            case 0x09:
                /* DAD B */
                setRegHL(add16(getRegHL(), getRegBC()));
                break;
            case 0x0A:
                /* LDAX B */
                memptr = getRegBC();
                regA = MemIoImpl.peek8(memptr);
                memptr++;
                break;
            case 0x0B:
                /* DEX B */
                decRegBC();
                break;
            case 0x0C:
                /* INR C */
                regC = inc8(regC);
                break;
            case 0x0D:
                /* DCR C */
                regC = dec8(regC);
                break;
            case 0x0E:
                /* MVI C */
                regC = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            case 0x0F:
                /* RRC */
                carryFlag = (regA & C_MASK) != 0;
                regA >>>= 1;
                if (carryFlag) {
                    regA |= 0x80;
                }
                break;
            case 0x10: {
                /* ARHL */
                int reg16 = getRegHL();
                int sign = reg16 & 0x8000;
                carryFlag = (reg16 & 0x01) != 0;
                reg16 = (reg16 >> 1) | sign;
                setRegHL(reg16 & 0xffff);
                break;
            }
            case 0x11:
                /* LXI D */
                setRegDE(MemIoImpl.peek16(regPC));
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0x12:
                /* STAX D */
                MemIoImpl.poke8(getRegDE(), regA);
                memptr = (regA << 8) | ((regE + 1) & 0xff);
                break;
            case 0x13: {
                /* INX D */
                incRegDE();
                break;
            }
            case 0x14: {
                /* INR D */
                regD = inc8(regD);
                break;
            }
            case 0x15: {
                /* DCR D */
                regD = dec8(regD);
                break;
            }
            case 0x16: {
                /* MVI D */
                regD = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0x17: {
                /* RAL */
                boolean oldCarry = carryFlag;
                carryFlag = (regA > 0x7f);
                regA = (regA << 1) & 0xff;
                if (oldCarry) {
                    regA |= 0x01;
                }
                break;
            }
            case 0x18: {
                /* RDEL */
                int reg16 = getRegDE();
                reg16 = reg16 << 1;
                if (carryFlag) {
                    reg16 |= 0x0001;
                }
                carryFlag = (reg16 & 0x10000) != 0;
                setRegDE(reg16 & 0xffff);
                break;
            }
            case 0x19: {
                /* DAD D */
                setRegHL(add16(getRegHL(), getRegDE()));
                break;
            }
            case 0x1A: {
                /* LDAX D */
                memptr = getRegDE();
                regA = MemIoImpl.peek8(memptr);
                memptr++;
                break;
            }
            case 0x1B: {
                /* DCX D */
                decRegDE();
                break;
            }
            case 0x1C: {
                /* INR E */
                regE = inc8(regE);
                break;
            }
            case 0x1D: {
                /* DCR E */
                regE = dec8(regE);
                break;
            }
            case 0x1E: {
                /* MVI E */
                regE = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0x1F: {
                /* RAR */
                boolean oldCarry = carryFlag;
                carryFlag = (regA & 0x01) != 0;
                regA >>>= 1;
                if (oldCarry) {
                    regA |= 0x80;
                }
                break;
            }
            case 0x20: {
                /* RIM */
                regA = MemIoImpl.inSerial() ? regSim | 0x80 : regSim & 0x7f;
                break;
            }
            case 0x21: {
                /* LXI H */
                setRegHL(MemIoImpl.peek16(regPC));
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0x22: {
                /* SHLD */
                memptr = MemIoImpl.peek16(regPC);
                MemIoImpl.poke16(memptr, getRegHL());
                memptr++;
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0x23: {
                /* INX HL */
                incRegHL();
                break;
            }
            case 0x24: {
                /* INR H */
                regH = inc8(regH);
                break;
            }
            case 0x25: {
                /* DCR H */
                regH = dec8(regH);
                break;
            }
            case 0x26: {
                /* MVI H */
                regH = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0x27: {
                /* DAA */
                daa();
                break;
            }
            case 0x28: {
                /* LDHI */
                int reg16 = MemIoImpl.peek8(regPC) & 0xff;
                regPC = (regPC + 1) & 0xffff;
                setRegDE((getRegHL() + reg16) & 0xffff);
                break;
            }
            case 0x29: {
                /* DAD H */
                work16 = getRegHL();
                setRegHL(add16(work16, work16));
                break;
            }
            case 0x2A: {
                /* LHLD */
                memptr = MemIoImpl.peek16(regPC);
                setRegHL(MemIoImpl.peek16(memptr));
                memptr++;
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0x2B: {
                /* DCX H */
                decRegHL();
                break;
            }
            case 0x2C: {
                /* INR L */
                regL = inc8(regL);
                break;
            }
            case 0x2D: {
                /* DCR L */
                regL = dec8(regL);
                break;
            }
            case 0x2E: {
                /* MVI L */
                regL = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0x2F: {
                /* CMA */
                regA ^= 0xff;
                break;
            }
            case 0x30: {
                /* SIM */
                int reg8 = MemIoImpl.peek8(regPC);
                if ((reg8 & 0x08) != 0) {
                    regSim = (regSim & 0xf8) | (reg8 & 0x07);
                }
                if ((reg8 & 0x40) != 0) {
                    regSim = (regSim & 0x7f) | (reg8 & 0x80);
                    MemIoImpl.outSerial((reg8 & 0x80) != 0);
                }
                break;
            }
            case 0x31: {
                /* LXI SP */
                regSP = MemIoImpl.peek16(regPC);
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0x32: {
                /* STA */
                memptr = MemIoImpl.peek16(regPC);
                MemIoImpl.poke8(memptr, regA);
                memptr = (regA << 8) | ((memptr + 1) & 0xff);
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0x33: {
                /* INX SP */
                incRegSP();
                break;
            }
            case 0x34: {
                /* INR M */
                work16 = getRegHL();
                work8 = inc8(MemIoImpl.peek8(work16));
                MemIoImpl.poke8(work16, work8);
                break;
            }
            case 0x35: {
                /* DCR M */
                work16 = getRegHL();
                work8 = dec8(MemIoImpl.peek8(work16));
                MemIoImpl.poke8(work16, work8);
                break;
            }
            case 0x36: {
                /* MVI M */
                MemIoImpl.poke8(getRegHL(), MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0x37: {
                /* STC */
                carryFlag = true;
                break;
            }
            case 0x38: {
                /* LDSI */
                int reg16 = MemIoImpl.peek8(regPC) & 0xff;
                regPC = (regPC + 1) & 0xffff;
                setRegDE((getRegSP() + reg16) & 0xffff);
                break;
            }
            case 0x39: {
                /* DAD SP */
                setRegHL(add16(getRegHL(), regSP));
                break;
            }
            case 0x3A: {
                /* LDA */
                memptr = MemIoImpl.peek16(regPC);
                regA = MemIoImpl.peek8(memptr);
                memptr++;
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0x3B: {
                /* DCX SP */
                decRegSP();
                break;
            }
            case 0x3C: {
                /* INR A */
                regA = inc8(regA);
                break;
            }
            case 0x3D: {
                /* DCR A */
                regA = dec8(regA);
                break;
            }
            case 0x3E: {
                /* MVI A */
                regA = MemIoImpl.peek8(regPC);
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0x3F: {
                /* CMC */
                carryFlag = !carryFlag;
                break;
            }
//            case 0x40: {     /* MOV B,B */
//                break;
//            }
            case 0x41: {
                /* MOV B,C */
                regB = regC;
                break;
            }
            case 0x42: {
                /* MOV B,D */
                regB = regD;
                break;
            }
            case 0x43: {
                /* MOV B,E */
                regB = regE;
                break;
            }
            case 0x44: {
                /* MOV B,H */
                regB = regH;
                break;
            }
            case 0x45: {
                /* MOV B,L */
                regB = regL;
                break;
            }
            case 0x46: {
                /* MOV B,M */
                regB = MemIoImpl.peek8(getRegHL());
                break;
            }
            case 0x47: {
                /* MOV B,A */
                regB = regA;
                break;
            }
            case 0x48: {
                /* MOV C,B */
                regC = regB;
                break;
            }
//            case 0x49: {     /* MOV C,C */
//                break;
//            }
            case 0x4A: {
                /* MOV C,D */
                regC = regD;
                break;
            }
            case 0x4B: {
                /* MOV C,E */
                regC = regE;
                break;
            }
            case 0x4C: {
                /* MOV C,H */
                regC = regH;
                break;
            }
            case 0x4D: {
                /* MOV C,L */
                regC = regL;
                break;
            }
            case 0x4E: {
                /* MOV C,M */
                regC = MemIoImpl.peek8(getRegHL());
                break;
            }
            case 0x4F: {
                /* MOV C,A */
                regC = regA;
                break;
            }
            case 0x50: {
                /* MOV D,B */
                regD = regB;
                break;
            }
            case 0x51: {
                /* MOV D,C */
                regD = regC;
                break;
            }
//            case 0x52: {     /* MOV D,D */
//                break;
//            }
            case 0x53: {
                /* MOV D,E */
                regD = regE;
                break;
            }
            case 0x54: {
                /* MOV D,H */
                regD = regH;
                break;
            }
            case 0x55: {
                /* MOV D,L */
                regD = regL;
                break;
            }
            case 0x56: {
                /* MOV D,M */
                regD = MemIoImpl.peek8(getRegHL());
                break;
            }
            case 0x57: {
                /* MOV D,A */
                regD = regA;
                break;
            }
            case 0x58: {
                /* MOV E,B */
                regE = regB;
                break;
            }
            case 0x59: {
                /* MOV E,C */
                regE = regC;
                break;
            }
            case 0x5A: {
                /* MOV E,D */
                regE = regD;
                break;
            }
//            case 0x5B: {     /* MOV E,E */
//                break;
//            }
            case 0x5C: {
                /* MOV E,H */
                regE = regH;
                break;
            }
            case 0x5D: {
                /* MOV E,L */
                regE = regL;
                break;
            }
            case 0x5E: {
                /* MOV E,M */
                regE = MemIoImpl.peek8(getRegHL());
                break;
            }
            case 0x5F: {
                /* MOV E,A */
                regE = regA;
                break;
            }
            case 0x60: {
                /* MOV H,B */
                regH = regB;
                break;
            }
            case 0x61: {
                /* MOV H,C */
                regH = regC;
                break;
            }
            case 0x62: {
                /* MOV H,D */
                regH = regD;
                break;
            }
            case 0x63: {
                /* MOV H,E */
                regH = regE;
                break;
            }
//            case 0x64: {     /* MOV H,H */
//                break;
//            }
            case 0x65: {
                /* MOV H,L */
                regH = regL;
                break;
            }
            case 0x66: {
                /* MOV H,M */
                regH = MemIoImpl.peek8(getRegHL());
                break;
            }
            case 0x67: {
                /* MOV H,A */
                regH = regA;
                break;
            }
            case 0x68: {
                /* MOV L,B */
                regL = regB;
                break;
            }
            case 0x69: {
                /* MOV L,C */
                regL = regC;
                break;
            }
            case 0x6A: {
                /* MOV L,D */
                regL = regD;
                break;
            }
            case 0x6B: {
                /* MOV L,E */
                regL = regE;
                break;
            }
            case 0x6C: {
                /* MOV L,H */
                regL = regH;
                break;
            }
//            case 0x6D: {     /* MOV L,L */
//                break;
//            }
            case 0x6E: {
                /* MOV L,M */
                regL = MemIoImpl.peek8(getRegHL());
                break;
            }
            case 0x6F: {
                /* MOV L,A */
                regL = regA;
                break;
            }
            case 0x70: {
                /* MOV M,B */
                MemIoImpl.poke8(getRegHL(), regB);
                break;
            }
            case 0x71: {
                /* MOV M,C */
                MemIoImpl.poke8(getRegHL(), regC);
                break;
            }
            case 0x72: {
                /* MOV M,D */
                MemIoImpl.poke8(getRegHL(), regD);
                break;
            }
            case 0x73: {
                /* MOV M,E */
                MemIoImpl.poke8(getRegHL(), regE);
                break;
            }
            case 0x74: {
                /* MOV M,H */
                MemIoImpl.poke8(getRegHL(), regH);
                break;
            }
            case 0x75: {
                /* MOV M,L */
                MemIoImpl.poke8(getRegHL(), regL);
                break;
            }
            case 0x76: {
                /* HLT */
                regPC = (regPC - 1) & 0xffff;
                halted = true;
                break;
            }
            case 0x77: {
                /* MOV M,A */
                MemIoImpl.poke8(getRegHL(), regA);
                break;
            }
            case 0x78: {
                /* MOV A,B */
                regA = regB;
                break;
            }
            case 0x79: {
                /* MOV A,C */
                regA = regC;
                break;
            }
            case 0x7A: {
                /* MOV A,D */
                regA = regD;
                break;
            }
            case 0x7B: {
                /* MOV A,E */
                regA = regE;
                break;
            }
            case 0x7C: {
                /* MOV A,H */
                regA = regH;
                break;
            }
            case 0x7D: {
                /* MOV A,L */
                regA = regL;
                break;
            }
            case 0x7E: {
                /* MOV A,M */
                regA = MemIoImpl.peek8(getRegHL());
                break;
            }
//            case 0x7F: {     /* MOV A,A */
//                break;
//            }
            case 0x80: {
                /* ADD B */
                add(regB);
                break;
            }
            case 0x81: {
                /* ADD C */
                add(regC);
                break;
            }
            case 0x82: {
                /* ADD D */
                add(regD);
                break;
            }
            case 0x83: {
                /* ADD E */
                add(regE);
                break;
            }
            case 0x84: {
                /* ADD H */
                add(regH);
                break;
            }
            case 0x85: {
                /* ADD L */
                add(regL);
                break;
            }
            case 0x86: {
                /* ADD M */
                add(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0x87: {
                /* ADD A */
                add(regA);
                break;
            }
            case 0x88: {
                /* ADC B */
                adc(regB);
                break;
            }
            case 0x89: {
                /* ADC C */
                adc(regC);
                break;
            }
            case 0x8A: {
                /* ADC D */
                adc(regD);
                break;
            }
            case 0x8B: {
                /* ADC E */
                adc(regE);
                break;
            }
            case 0x8C: {
                /* ADC H */
                adc(regH);
                break;
            }
            case 0x8D: {
                /* ADC L */
                adc(regL);
                break;
            }
            case 0x8E: {
                /* ADC M */
                adc(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0x8F: {
                /* ADC A */
                adc(regA);
                break;
            }
            case 0x90: {
                /* SUB B */
                sub(regB);
                break;
            }
            case 0x91: {
                /* SUB C */
                sub(regC);
                break;
            }
            case 0x92: {
                /* SUB D */
                sub(regD);
                break;
            }
            case 0x93: {
                /* SUB E */
                sub(regE);
                break;
            }
            case 0x94: {
                /* SUB H */
                sub(regH);
                break;
            }
            case 0x95: {
                /* SUB L */
                sub(regL);
                break;
            }
            case 0x96: {
                /* SUB M */
                sub(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0x97: {
                /* SUB A */
                sub(regA);
                break;
            }
            case 0x98: {
                /* SBB B */
                sbc(regB);
                break;
            }
            case 0x99: {
                /* SBB C */
                sbc(regC);
                break;
            }
            case 0x9A: {
                /* SBB D */
                sbc(regD);
                break;
            }
            case 0x9B: {
                /* SBB E */
                sbc(regE);
                break;
            }
            case 0x9C: {
                /* SBB H */
                sbc(regH);
                break;
            }
            case 0x9D: {
                /* SBB L */
                sbc(regL);
                break;
            }
            case 0x9E: {
                /* SBB M */
                sbc(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0x9F: {
                /* SBB A */
                sbc(regA);
                break;
            }
            case 0xA0: {
                /* ANA B */
                and(regB);
                break;
            }
            case 0xA1: {
                /* ANA C */
                and(regC);
                break;
            }
            case 0xA2: {
                /* ANA D */
                and(regD);
                break;
            }
            case 0xA3: {
                /* ANA E */
                and(regE);
                break;
            }
            case 0xA4: {
                /* ANA H */
                and(regH);
                break;
            }
            case 0xA5: {
                /* ANA L */
                and(regL);
                break;
            }
            case 0xA6: {
                /* ANA M */
                and(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0xA7: {
                /* ANA A */
                and(regA);
                break;
            }
            case 0xA8: {
                /* XRA B */
                xor(regB);
                break;
            }
            case 0xA9: {
                /* XRA C */
                xor(regC);
                break;
            }
            case 0xAA: {
                /* XRA D */
                xor(regD);
                break;
            }
            case 0xAB: {
                /* XRA E */
                xor(regE);
                break;
            }
            case 0xAC: {
                /* XRA H */
                xor(regH);
                break;
            }
            case 0xAD: {
                /* XRA L */
                xor(regL);
                break;
            }
            case 0xAE: {
                /* XRA M */
                xor(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0xAF: {
                /* XRA A */
                xor(regA);
                break;
            }
            case 0xB0: {
                /* ORA B */
                or(regB);
                break;
            }
            case 0xB1: {
                /* ORA C */
                or(regC);
                break;
            }
            case 0xB2: {
                /* ORA D */
                or(regD);
                break;
            }
            case 0xB3: {
                /* ORA E */
                or(regE);
                break;
            }
            case 0xB4: {
                /* ORA H */
                or(regH);
                break;
            }
            case 0xB5: {
                /* ORA L */
                or(regL);
                break;
            }
            case 0xB6: {
                /* ORA M */
                or(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0xB7: {
                /* ORA A */
                or(regA);
                break;
            }
            case 0xB8: {
                /* CMP B */
                cp(regB);
                break;
            }
            case 0xB9: {
                /* CMP C */
                cp(regC);
                break;
            }
            case 0xBA: {
                /* CMP D */
                cp(regD);
                break;
            }
            case 0xBB: {
                /* CMP E */
                cp(regE);
                break;
            }
            case 0xBC: {
                /* CMP H */
                cp(regH);
                break;
            }
            case 0xBD: {
                /* CMP L */
                cp(regL);
                break;
            }
            case 0xBE: {
                /* CMP M */
                cp(MemIoImpl.peek8(getRegHL()));
                break;
            }
            case 0xBF: {
                /* CMP A */
                cp(regA);
                break;
            }
            case 0xC0: {
                /* RNZ */
                if ((Flags & Z_MASK) == 0) {
                    regPC = memptr = pop();
                }
                break;
            }
            case 0xC1: {
                /* POP B */
                setRegBC(pop());
                break;
            }
            case 0xC2: {
                /* JNZ */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & Z_MASK) == 0) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xC3: {
                /* JMP */
                memptr = regPC = MemIoImpl.peek16(regPC);
                break;
            }
            case 0xC4: {
                /* CNZ */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & Z_MASK) == 0) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xC5: {
                /* PUSH B */
                push(getRegBC());
                break;
            }
            case 0xC6: {
                /* ADI */
                add(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0xC7: {
                /* RST 0 */
                push(regPC);
                regPC = memptr = 0x00;
                break;
            }
            case 0xC8: {
                /* RZ */
                if ((Flags & Z_MASK) != 0) {
                    regPC = memptr = pop();
                }
                break;
            }
            case 0xC9: {
                /* RET */
                regPC = memptr = pop();
                break;
            }
            case 0xCA: {
                /* JZ */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & Z_MASK) != 0) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xCB: {
                /* RSTV */
                if (VFlag) {
                    push(regPC);
                    regPC = memptr = 0x40;
                }
                break;
            }
            case 0xCC: {
                /* CZ */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & Z_MASK) != 0) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xCD: {
                /* CALL */
                memptr = MemIoImpl.peek16(regPC);
                push(regPC + 2);
                regPC = memptr;
                break;
            }
            case 0xCE: {
                /* ACI */
                adc(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0xCF: {
                /* RST 1 */
                push(regPC);
                regPC = memptr = 0x08;
                break;
            }
            case 0xD0: {
                /* RNC */
                if (!carryFlag) {
                    regPC = memptr = pop();
                }
                break;
            }
            case 0xD1: {
                /* POP D */
                setRegDE(pop());
                break;
            }
            case 0xD2: {
                /* JNC */
                memptr = MemIoImpl.peek16(regPC);
                if (!carryFlag) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xD3: {
                /* OUT */
                work8 = MemIoImpl.peek8(regPC);
                memptr = regA << 8;
                MemIoImpl.outPort(memptr | work8, regA);
                memptr |= ((work8 + 1) & 0xff);
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0xD4: {
                /* CNC */
                memptr = MemIoImpl.peek16(regPC);
                if (!carryFlag) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xD5: {
                /* PUSH D */
                push(getRegDE());
                break;
            }
            case 0xD6: {
                /* SUI */
                sub(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0xD7: {
                /* RST 2 */
                push(regPC);
                regPC = memptr = 0x10;
                break;
            }
            case 0xD8: {
                /* RC */
                if (carryFlag) {
                    regPC = memptr = pop();
                }
                break;
            }
            case 0xD9: {
                /* SHLX */
                memptr = getRegDE();
                MemIoImpl.poke16(memptr, getRegHL());
                memptr++;
                break;
            }
            case 0xDA: {
                /* JC */
                memptr = MemIoImpl.peek16(regPC);
                if (carryFlag) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xDB: {
                /* IN  */
                memptr = (regA << 8) | MemIoImpl.peek8(regPC);
                regA = MemIoImpl.inPort(memptr);
                memptr++;
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0xDC: {
                /* CC */
                memptr = MemIoImpl.peek16(regPC);
                if (carryFlag) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xDD: {
                /* JNUI */
                memptr = MemIoImpl.peek16(regPC);
                if (!UFlag) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            }
            case 0xDE: {
                /* SBI */
                sbc(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            }
            case 0xDF: {
                /* RST 3 */
                push(regPC);
                regPC = memptr = 0x18;
                break;
            }
            case 0xE0:
                /* RPO */
                if ((Flags & P_MASK) == 0) {
                    regPC = memptr = pop();
                }
                break;
            case 0xE1:
                /* POP H */
                setRegHL(pop());
                break;
            case 0xE2:
                /* JPO */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & P_MASK) == 0) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xE3:
                /* XTHL (SP),HL */
                work16 = regH;
                work8 = regL;
                setRegHL(MemIoImpl.peek16(regSP));
                MemIoImpl.poke8((regSP + 1) & 0xffff, work16);
                MemIoImpl.poke8(regSP, work8);
                memptr = getRegHL();
                break;
            case 0xE4:
                /* CPO */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & P_MASK) == 0) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xE5:
                /* PUSH H */
                push(getRegHL());
                break;
            case 0xE6:
                /* ANI */
                and(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            case 0xE7:
                /* RST 4 */
                push(regPC);
                regPC = memptr = 0x20;
                break;
            case 0xE8:
                /* RPE */
                if ((Flags & P_MASK) != 0) {
                    regPC = memptr = pop();
                }
                break;
            case 0xE9:
                /* PCHL */
                regPC = getRegHL();
                break;
            case 0xEA:
                /* JPE */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & P_MASK) != 0) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xEB:
                /* XCHG */
                work8 = regH;
                regH = regD;
                regD = work8;

                work8 = regL;
                regL = regE;
                regE = work8;
                break;
            case 0xEC:
                /* CPE */
                memptr = MemIoImpl.peek16(regPC);
                if ((Flags & P_MASK) != 0) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xED:
                /* LHLX */
                memptr = getRegDE();
                setRegHL(MemIoImpl.peek16(memptr));
                memptr++;
                break;
            case 0xEE:
                /* XRI */
                xor(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            case 0xEF:
                /* RST 5 */
                push(regPC);
                regPC = memptr = 0x28;
                break;
            case 0xF0:
                /* RP */
                if (Flags < S_MASK) {
                    regPC = memptr = pop();
                }
                break;
            case 0xF1:
                /* POP PSW */
                setRegAF(pop());
                break;
            case 0xF2:
                /* JP P,nn */
                memptr = MemIoImpl.peek16(regPC);
                if (Flags < S_MASK) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xF3:
                /* DI */
                regSim &= ~0x08;
                break;
            case 0xF4:
                /* CP */
                memptr = MemIoImpl.peek16(regPC);
                if (Flags < S_MASK) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xF5:
                /* PUSH PSW */
                push(getRegAF());
                break;
            case 0xF6:
                /* ORI */
                or(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            case 0xF7:
                /* RST 6 */
                push(regPC);
                regPC = memptr = 0x30;
                break;
            case 0xF8:
                /* RM */
                if (Flags > 0x7f) {
                    regPC = memptr = pop();
                }
                break;
            case 0xF9:
                /* SPHL */
                regSP = getRegHL();
                break;
            case 0xFA:
                /* JM */
                memptr = MemIoImpl.peek16(regPC);
                if (Flags > 0x7f) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xFB:
                /* EI */
                regSim |= 0x08;
                pendingEI = true;
                break;
            case 0xFC:
                /* CM */
                memptr = MemIoImpl.peek16(regPC);
                if (Flags > 0x7f) {
                    push(regPC + 2);
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xFD:
                /* JUI */
                memptr = MemIoImpl.peek16(regPC);
                if (UFlag) {
                    regPC = memptr;
                    break;
                }
                regPC = (regPC + 2) & 0xffff;
                break;
            case 0xFE:
                /* CPI */
                cp(MemIoImpl.peek8(regPC));
                regPC = (regPC + 1) & 0xffff;
                break;
            case 0xFF:
                /* RST 7 */
                push(regPC);
                regPC = memptr = 0x38;
        } // switch
    }

}
