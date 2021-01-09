/*	Class for emulation of PIO 8255 chip
	Copyright (c) 2006 Roman Borik <pmd85emu@gmail.com>

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General final License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General final License for more details.

	You should have received a copy of the GNU General final License
	along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package machine;

final class Pio8255 {

    public final int PP_PortA   = 0;
    public final int PP_PortB   = 1;
    public final int PP_PortC   = 2;
    public final int PP_CWR     = 3;
    public final int PP_PortCH  = 4;
    public final int PP_PortCL  = 5;
    
    private final int PP_Bit0   = 0;
    private final int PP_Bit1   = 1;
    private final int PP_Bit2   = 2;
    private final int PP_Bit3   = 3;
    private final int PP_Bit4   = 4;
    private final int PP_Bit5   = 5;
    private final int PP_Bit6   = 6;
    private final int PP_Bit7   = 7;
//---------------------------------------------------------------------------
    private final int CWR_MASK  = 0x80;  // maska zapisu CWR

// skupina A
    private final int GA_MODE   = 0x60;
    private final int GA_MODE0  = 0x00;
    private final int GA_MODE1  = 0x20;
    private final int GA_MODE2  = 0x40;
    private final int PORTA_DIR = 0x10;
    private final int PORTA_INP = 0x10;
    private final int PORTA_OUT = 0x00;
    private final int PORTCH_DIR= 0x08;
    private final int PORTCH_INP= 0x08;
    private final int PORTCH_OUT= 0x00;

// skupina B
    private final int GB_MODE   = 0x04;
    private final int GB_MODE0  = 0x00;
    private final int GB_MODE1  = 0x04;
    private final int PORTB_DIR = 0x02;
    private final int PORTB_INP = 0x02;
    private final int PORTB_OUT = 0x00;
    private final int PORTCL_DIR= 0x01;
    private final int PORTCL_INP= 0x01;
    private final int PORTCL_OUT= 0x00;

// nastavenie CWR po resete - vsetky porty su v mode 0 ako vstupy
private final int BASIC_CWR     = 
        (CWR_MASK | GA_MODE0 | PORTA_INP | PORTCH_INP | GB_MODE0 | PORTB_INP | PORTCL_INP);
//---------------------------------------------------------------------------
private final int _STBA         = PP_Bit4;         // Strobe A - vstup
private final int _STBA_MASK    = (1 << _STBA);
private final int IBFA          = PP_Bit5;         // Input buffer A full - vystup
private final int IBFA_MASK     = (1 << IBFA);
private final int _OBFA         = PP_Bit7;         // Output buffer A full - vystup
private final int _OBFA_MASK    = (1 << _OBFA);
private final int _ACKA         = PP_Bit6;         // Acknowledge A - vstup
private final int _ACKA_MASK    = (1 << _ACKA);
private final int INTRA         = PP_Bit3;         // Interrupt request A - vystup
private final int INTRA_MASK    = (1 << INTRA);
private final int INTEAIN       = PP_Bit4;         // Interrupt enable A input - status
private final int INTEAIN_MASK  = (1 << INTEAIN);
private final int INTEAOUT      = PP_Bit6;         // Interrupt enable A output - status
private final int INTEAOUT_MASK = (1 << INTEAOUT);

private final int _STBB         = PP_Bit2;         // Strobe B - vstup
private final int _STBB_MASK    = (1 << _STBB);
private final int IBFB          = PP_Bit1;         // Input buffer B full - vystup
private final int IBFB_MASK     = (1 << IBFB);
private final int _OBFB         = PP_Bit1;         // Output buffer B full - vystup
private final int _OBFB_MASK    = (1 << _OBFB);
private final int _ACKB         = PP_Bit2;         // Acknowledge B - vstup
private final int _ACKB_MASK    = (1 << _ACKB);
private final int INTRB         = PP_Bit0;         // Interrupt request B - vystup
private final int INTRB_MASK    = (1 << INTRB);
private final int INTEB         = PP_Bit2;         // Interrupt enable B - status
private final int INTEB_MASK    = (1 << INTEB);
//---------------------------------------------------------------------------

	// interne registre
	private int CWR;
	private int InBufferA;
	private int InLatchA;
	private int OutLatchA;
	private int InBufferB;
	private int InLatchB;
	private int OutLatchB;
	private int InBufferC;
	private int OutLatchC;

	// INTE klopne obvody (Flip-Flop)
	// povolenie prerusenia v modoch 1 a 2
	private boolean InteAin;  // PC4
	private boolean InteAout; // PC6
	private boolean InteB;    // PC2
        
        private static Pio8255Notify on;
//---------------------------------------------------------------------------
        
/**
 * Konstruktor pre vytvorenie objektu cipu PIO 8255. Zodpoveda stavu Power-up,
 * teda pripojeniu napajania.
 * Na rozdiel od originalneho cipu, ktory je po pripojeni napajania v neurcitom
 * stave, je mozne v konstruktore previest ihned reset cipu.
 * Zaroven sa zrusia vsetky notifykacne funkcie.

 * @param reset ak je true, prevedie sa zaroven reset cipu
 */
public Pio8255(Pio8255Notify notify)
{
    on = notify;
    Reset();
}
//---------------------------------------------------------------------------

/**
 * Metoda ChipReset prevedie reset cipu. Zodpoveda privedeniu urovne H na vstup
 * RESET (35).
 * Volitelne je mozne zrusit vsetky notifikacne funkcie.
 *
 * @param clearNotifyFunc ak je true, zrusi vsetky notifykacne funkcie
 */
public void Reset()
{
        // nastav vsetky porty do modu 0
        // vynuluj vsetky vnutorne registre
        CpuWrite(PP_CWR, BASIC_CWR); // b10011011
        InLatchA = 0; // samotne nastavenie rezimu nenuluje vstupny latch
        InLatchB = 0;
}
//---------------------------------------------------------------------------
/**
 * Metoda GetChipState je pouzivana pri vytvarani Snapshotu a ulozi niektore
 * vnutorne registre chipu do buffra. Ak je buffer null, vrati potrebnu velkost
 * buffra v bytoch.
 * Data sa do buffra ulozia v poradi: CWR, PC, PB, PA, prerusenia
 *
 * @return int[5] buffer se stavem
 */
public int[] getState()
{
        int[] res = new int[5];
    
        res[0] = CWR;
	res[1] = OutLatchC;
	res[2] = OutLatchB;
	res[3] = OutLatchA;
	res[4] = (int) (
		((InteAin == true) ? 1 : 0) |
		((InteAout == true) ? 2 : 0) |
		((InteB == true) ? 4 : 0)
		);

	return res;
}
//---------------------------------------------------------------------------
/**
 * Metoda SetChipState je pouzivana po otvoreni Snapshotu pre prednastavenie
 * niektorych vnutornych registrov chipu.
 * Data v buffri musia byt v poradi: CWR, PC, PB, PA, prerusenia
 *
 * @param buffer buffer se stavem
 */
public void setState(int state[])
{
	if (state != null) {
		Reset();
		CpuWrite(PP_CWR, state[0]);
		CpuWrite(PP_PortC, state[1]);
		CpuWrite(PP_PortB, state[2]);
		CpuWrite(PP_PortA, state[3]);
		if ((state[4] & 1) != 0)
			CpuWrite(PP_CWR, 9);  // InteAin - PC4 <- 1
		if ((state[4] & 2) != 0)
			CpuWrite(PP_CWR, 13); // InteAout - PC6 <- 1
		if ((state[4] & 4) != 0)
			CpuWrite(PP_CWR, 5);  // InteB - PC2 <- 1
	}
}
//---------------------------------------------------------------------------
/**
 * Privatna metoda NotifyOnWritePortC pozivana pri zapise na port C procesorom.
 * K notifikacii dojde iba v pripade, ze sa vystupna hodnota na porte C zmenila.
 * POZOR: Notifikacia je prevedena bud volanim funkcie OnCpuWriteC alebo dvojice
 * OnCpuWriteCH a OnCpuWriteCL. OnCpuWriteC ma vyssiu prioritu. Teda, ak je
 * nastavena adresa funkcie OnCpuWriteC, notifikacne funkcie pre polovice portu
 * su ignorovane.
 *
 * @param oldVal povodna hodnota na vystupe portu C
 * @param newVal nova hodnota posielana na vystup portu C
 */
private void NotifyOnWritePortC(int oldVal, int newVal)
{
	int val;

	val = oldVal ^ newVal;
	if (val!=0)
		on.OnCpuWriteC();
	else {
		if ((val & 0xF0) !=0)
			on.OnCpuWriteCH();
		if ((val & 0x0F) !=0)
			on.OnCpuWriteCL();
	}
}
//---------------------------------------------------------------------------
/**
 * Metodu CpuWrite vola mikroprocesor pri vykonavani instrukcie OUT - zapis na
 * port (CPU -> PIO). Zodpoveda teda privedeniu urovne L na vstupy /CS (6) a
 * /WR (36).
 * PO zapisani hodnoty 'val' na cielovy port 'dest' zavola prislusnu
 * notifikacnu funkciu (ak bola konkretnou periferiou nastavena), aby mohla
 * periferia data spracovat. Pri portoch A a B sa to tyka, iba ak su v Mode 0.
 * V Modoch 1 a 2 je periferia notifikovana notifikacnymi funkciami portu C
 * prostrednictvom handshake signalov.
 *
 * @param dest oznacuje port (TPIOPort), na ktory je posielana hodnota 'val'
 * @param val hodnota posielana na dany port
 */
public void CpuWrite(int dest, int val)
{
	int oldVal;
	int mode;

	switch (dest) {
		case PP_PortA :
			if ((CWR & GA_MODE) == GA_MODE0) { // Mod 0
				// zmena hodnoty
				OutLatchA = val;
				on.OnCpuWriteA();  // notifikacia
			}
			else { // Mod 1, 2
				OutLatchA = val;
				oldVal = OutLatchC;
				OutLatchC &= (int)(~(INTRA_MASK | _OBFA_MASK));
				// notifikacia
				NotifyOnWritePortC(oldVal, OutLatchC);
			}
			break;

		case PP_PortB :
			if ((CWR & GB_MODE) == GB_MODE0) { // Mod 0
				// zmena hodnoty
				OutLatchB = val;
				on.OnCpuWriteB();  // notifikacia
			}
			else { // Mod 1
				OutLatchB = val;
				oldVal = OutLatchC;
				OutLatchC &= ~(INTRB_MASK | _OBFB_MASK);
				// notifikacia
				NotifyOnWritePortC(oldVal, OutLatchC);
			}
			break;

		case PP_PortC :
			// ovplyvnene su iba vystupne bity v Mode 0
			oldVal = OutLatchC;

			if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT)
					&& (CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT))
				OutLatchC = val;
			else if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT))
				OutLatchC = (int)((OutLatchC & 0xF0) | (val & 0x0F));
			else if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT))
				OutLatchC = (int)((OutLatchC & 0x0F) | (val & 0xF0));

			// notifikacia
			NotifyOnWritePortC(oldVal, OutLatchC);
			break;

		case PP_CWR :
			if ((val & CWR_MASK) !=0) {
				// nastavenie rezimu PIO
				CWR = val;
				if ((CWR & GA_MODE) == GA_MODE)
					CWR &= ~GA_MODE1;

				// vynuluj vsetky vnutorne registre
				InBufferA = 0xFF; // Pull-up
//				InLatchA = 0; nastavenie rezimu nenuluje vstupny latch
				OutLatchA = 0;

				InBufferB = 0xFF; // Pull-up
				if ((CWR & GB_MODE) != GB_MODE0)
					InLatchB = 0;
				OutLatchB = 0;

				InBufferC = 0;
				if ((CWR & GB_MODE) == GB_MODE0)
					InBufferC |= 0x07; // Pull-up
				if ((CWR & GA_MODE) == GA_MODE0)
					InBufferC |= 0xF8;

//				oldVal = OutLatchC;
				OutLatchC = 0;
				if ((CWR & GB_MODE) != GB_MODE0)
					OutLatchC |= _OBFB_MASK;
				if ((CWR & GA_MODE) != GA_MODE0)
					OutLatchC |= _OBFA_MASK;

				// zakaz preruseni
				InteAin = false;
				InteAout = false;
				InteB = false;

				on.OnCpuWriteCWR(CWR);
/*
				if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT)
						&& (CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT))
					oldVal = ~OutLatchC;
				else {
					if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT))
						oldVal = OutLatchC ^ 0x07;
					if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT))
						oldVal = OutLatchC ^ 0xF8;
				}
				NotifyOnWritePortC(oldVal, OutLatchC);
*/
				NotifyOnWritePortC((int)(~OutLatchC), OutLatchC);
			}
			else {
				// nastavenie bitov portu C
				boolean inte = false;
				val &= 0x0F;

				mode = (int)(CWR & (GA_MODE | PORTA_DIR));
				oldVal = OutLatchC;

				if ((mode == (GA_MODE1 | PORTA_INP) || (CWR & GA_MODE) == GA_MODE2) && (val & 0x0E) == 8) {
					InteAin = ((val & 1)!=0);
					inte = true;
//					debug("ChipPIO8255", "InteAin=%d", InteAin);
				}
				else if ((mode == (GA_MODE1 | PORTA_OUT) || (CWR & GA_MODE) == GA_MODE2) && (val & 0x0E) == 12) {
					InteAout = ((val & 1)!=0);
					inte = true;
//					debug("ChipPIO8255", "InteAout=%d", InteAout);
				}

				if (inte == true) {
					OutLatchC &= ~INTRA_MASK;
					if ((InteAin == true && (InBufferC & _STBA_MASK) == _STBA_MASK
							 && (OutLatchC & IBFA_MASK) == IBFA_MASK)
							|| (InteAout == true && (InBufferC & _ACKA_MASK) == _ACKA_MASK
									&& (OutLatchC & _OBFA_MASK) == _OBFA_MASK)) {
						OutLatchC |= INTRA_MASK;
					}
//					debug("ChipPIO8255", "OutLatchC=%u, InBufferC=%u", OutLatchC, InBufferC);
				}

				if ((CWR & GB_MODE) == GB_MODE1 && (val & 0x0E) == 4) {
					InteB = ((val & 1)!=0);
					OutLatchC &= ~INTRB_MASK;
					if (InteB == true) {
						if (((CWR & PORTB_INP) == PORTB_INP
								 && (InBufferC & _STBB_MASK) == _STBB_MASK
								 && (OutLatchC & IBFB_MASK) == IBFB_MASK)
								|| ((CWR & PORTB_OUT) == PORTB_OUT
										&& (InBufferC & _ACKB_MASK) == _ACKB_MASK
										&& (OutLatchC & _OBFB_MASK) == _OBFB_MASK)) {
							OutLatchC |= INTRB_MASK;
						}
					}
					inte = true;
				}

				if (inte == false) {
					if (((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT)
						 && (CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT))
						|| ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT)
								&& (CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_INP)
								&& (val >>> 1) < 4)
						|| ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_INP)
								&& (CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT)
								&& (val >>> 1) > 3)
						|| ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT)
								&& (val >>> 1) < 3)
						|| ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT)
								&& (val >>> 1) > 2))
					{
						if ((val & 1)!=0)
							OutLatchC |= (int)((1 << (val >>> 1)));
						else
							OutLatchC &= (int)(~(1 << (val >>> 1)));
					}
				}

				NotifyOnWritePortC(oldVal, OutLatchC);
			}
			break;

		default :
//			warning("ChipPIO8255", "CpuWrite > invalid PIO port: %d", dest);
			break;
	}
}
//---------------------------------------------------------------------------
/**
 * Metodu CpuRead vola mikroprocesor pri vykonavani instrukcie IN - citanie z
 * portu (CPU <- PIO). Zodpoveda teda privedeniu urovne L na vstupy /CS (6) a
 * /RD (5).
 * PRED precitanim hodnoty zo zdrojoveho portu 'src' zavola prislusnu
 * notifikacnu funkciu (ak bola konkretnou periferiou nastavena), aby mohla
 * periferia potrebne data pripravit. Pri portoch A a B sa to tyka, iba ak su
 * v Mode 0. V Modoch 1 a 2 je periferia notifikovana notifikacnymi funkciami
 * portu C prostrednictvom handshake signalov.
 *
 * @param src oznacuje port (TPIOPort), z ktoreho sa ma udaj precitat
 * @return hodnota precitana z portu
 */
public int CpuRead(int src)
{
    int ret_val = 0;
    int oldVal;
    int mode;

    switch (src) {
        case PP_PortA :
            mode = (int)(CWR & (GA_MODE | PORTA_DIR));

            // notifikacia je len v mode 0, vstup
            // v ostatnych modoch sa uplatnuju handshake signaly
            if (mode == (GA_MODE0 | PORTA_INP))
                on.OnCpuReadA();

            if (mode == (GA_MODE0 | PORTA_INP))  // Mod 0, Vstup
                ret_val = InBufferA;  // precitanie hodnoty zo vstupu
            if (mode == (GA_MODE0 | PORTA_OUT))  // Mod 0, Vystup
            // precitanie hodnoty vystupneho latchu s ohladom na stav vstupnych liniek
                ret_val = OutLatchA & InBufferA;
            if (mode == (GA_MODE1 | PORTA_OUT))  // Mod 1, Vystup
                ret_val = OutLatchA;  // precitanie hodnoty vystupneho latchu
            if (mode == (GA_MODE1 | PORTA_INP) // Mod 1, Vstup
             || (CWR & GA_MODE) == GA_MODE2) { // alebo Mod 2
                ret_val = InLatchA; // precitanie hodnoty, ktoru dodala periferia
                // signal /RD nuluje bity INTRA a IBFA
                oldVal = OutLatchC;
                OutLatchC &= ~(INTRA_MASK | IBFA_MASK);
                NotifyOnWritePortC(oldVal, OutLatchC);
            }
            break;

        case PP_PortB :
            mode = (int)(CWR & (GB_MODE | PORTB_DIR));

            // notifikacia je len v mode 0, vstup
            // v ostatnych modoch sa uplatnuju handshake signaly
            if (mode == (GB_MODE0 | PORTB_INP))
                on.OnCpuReadB();

            if (mode == (GB_MODE0 | PORTB_OUT)) // Mod 0, Vystup
                ret_val = OutLatchB;  // precitanie hodnoty vystupneho latchu
            if (mode == (GB_MODE0 | PORTB_INP)) // Mod 0, Vstup
                ret_val = InBufferB;  // precitanie hodnoty zo vstupu
            if (mode == (GB_MODE1 | PORTB_OUT))  // Mod 1, Vystup
                ret_val = OutLatchB;  // precitanie hodnoty vystupneho latchu
            if (mode == (GB_MODE1 | PORTB_INP)) {  // Mod 1, Vstup
                ret_val = InLatchB; // precitanie hodnoty, ktoru dodala periferia
                // signal /RD nuluje bity INTRB a IBFB
                oldVal = OutLatchC;
                OutLatchC &= ~(INTRB_MASK | IBFB_MASK);
                NotifyOnWritePortC(oldVal, OutLatchC);
            }
            break;

        case PP_PortC :
            ret_val = 0;

            // Mod 0, Vstup
            if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_INP))
                on.OnCpuReadCH();

            if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_INP))
                on.OnCpuReadCL();
			
            // Mod 0, Vstup
            if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_INP)
            || (CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_INP))
                on.OnCpuReadC();
			

            if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT)) // Mod 0, Vystup
                ret_val |= (int)(OutLatchC & 0x0F);
            if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_INP)) // Mod 0, Vstup
                ret_val |= (int)(InBufferC & 0x0F);
            if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT)) // Mod 0, Vystup
                ret_val |= (int)(OutLatchC & 0xF0);
            if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_INP)) // Mod 0, Vstup
                ret_val |= (int)(InBufferC & 0xF0);
       
            switch (CWR & GA_MODE) {
                case GA_MODE1 :
                    if ((CWR & PORTA_DIR) == PORTA_INP) {
                        ret_val &= 0xC7;
                        if (InteAin == true)
                            ret_val |= INTEAIN_MASK;
                        ret_val |= (int)(OutLatchC & IBFA_MASK);
                    }
                    else {
                        ret_val &= 0x37;
                        if (InteAout == true)
                            ret_val |= INTEAOUT_MASK;
                        ret_val |= (int)(OutLatchC & _OBFA_MASK);
                    }
                    ret_val |= (int)(OutLatchC & INTRA_MASK);
                    break;

                case GA_MODE2 :
                    ret_val &= 0x07;
                    if (InteAin == true)
                        ret_val |= INTEAIN_MASK;
                    ret_val |= (int)(OutLatchC & IBFA_MASK);
                    if (InteAout == true)
                        ret_val |= INTEAOUT_MASK;
                    ret_val |= (int)(OutLatchC & _OBFA_MASK);
                    ret_val |= (int)(OutLatchC & INTRA_MASK);
                    break;
            }

            if ((CWR & GB_MODE) == GB_MODE1) {
                ret_val &= 0xF8;
                if (InteB == true)
                    ret_val |= INTEB_MASK;
                ret_val |= (int)(OutLatchC & INTRB_MASK);
                if ((CWR & PORTB_DIR) == PORTB_INP)
                    ret_val |= (int)(OutLatchC & IBFB_MASK);
                else
                    ret_val |= (int)(OutLatchC & _OBFB_MASK);
                if ((CWR & GA_MODE) == GA_MODE0) {
                    ret_val &= 0xF7;
                    if ((CWR & PORTCH_DIR) == PORTCH_INP)
                        ret_val |= (int)(InBufferC & 0x08);
                    else
                        ret_val |= (int)(OutLatchC & 0x08);
                }
            } 
            break;

        case PP_CWR :     // NMOS verzia 8255 neumoznuje citanie CWR
            ret_val = CWR;  // toto je mozne len v CMOS verzii 82C55
            break;

        default :
//          warning("PIO8255", "CpuRead > invalid PIO port: %d", src);
            ret_val = 0;
            break; 
    }
    return ret_val;
}
//---------------------------------------------------------------------------
public void PeripheralWriteByte(int dest, int val)
{
	int oldVal;

	switch (dest) {
		case PP_PortA :
			InBufferA = val;
			break;

		case PP_PortB :
			InBufferB = val;
			break;

		case PP_PortC :
			oldVal = OutLatchC;
			if ((CWR & GB_MODE) == GB_MODE1) {
				// vstup
				if ((CWR & PORTB_DIR) == PORTB_INP) {
					if ((InBufferC & _STBB_MASK) == _STBB_MASK && (val & _STBB_MASK) == 0) {
						// /STB  --__
						InLatchB = InBufferB;
						OutLatchC |= IBFB_MASK;
						OutLatchC &= ~INTRB_MASK;
					}
					else if ((InBufferC & _STBB_MASK) == 0 && (val & _STBB_MASK) == _STBB_MASK) {
						// /STB  __--
						if (InteB == true && (OutLatchC & IBFB_MASK) == IBFB_MASK)
							OutLatchC |= INTRB_MASK;
						else
							OutLatchC &= ~INTRB_MASK;
					}
				}
				else { // vystup
					if ((InBufferC & _ACKB_MASK) == _ACKB_MASK && (val & _ACKB_MASK) == 0) {
						// /ACK  --__
						OutLatchC |= _OBFB_MASK;
						OutLatchC &= ~INTRB_MASK;
					}
					else if ((InBufferC & _ACKB_MASK) == 0 && (val & _ACKB_MASK) == _ACKB_MASK) {
						// /ACK  __--
						if (InteB == true && (OutLatchC & _OBFB_MASK) == _OBFB_MASK)
							OutLatchC |= INTRB_MASK;
						else
							OutLatchC &= ~INTRB_MASK;
					}
				}
			}

			if ((CWR & GA_MODE) != GA_MODE0) {
				// vstup
				if ((CWR & GA_MODE) == GA_MODE2
						|| (CWR & (GA_MODE | PORTA_DIR)) == (GA_MODE1 | PORTA_INP)) {
					if ((InBufferC & _STBA_MASK) == _STBA_MASK && (val & _STBA_MASK) == 0) {
						// /STB  --__
						InLatchA = InBufferA;
						OutLatchC |= IBFA_MASK;
						OutLatchC &= ~INTRA_MASK;
					}
					else if ((InBufferC & _STBA_MASK) == 0 && (val & _STBA_MASK) == _STBA_MASK) {
						// /STB  __--
						if (InteAin == true && (OutLatchC & IBFA_MASK) == IBFA_MASK)
							OutLatchC |= INTRA_MASK;
						else
							OutLatchC &= ~INTRA_MASK;
					}
				}

				// vystup
				if ((CWR & GA_MODE) == GA_MODE2
						|| (CWR & (GA_MODE | PORTA_DIR)) == (GA_MODE1 | PORTA_OUT)) {
					if ((InBufferC & _ACKA_MASK) == _ACKA_MASK && (val & _ACKA_MASK) == 0) {
						// /ACK  --__
						OutLatchC |= _OBFA_MASK;
						OutLatchC &= ~INTRA_MASK;
					}
					else if ((InBufferC & _ACKA_MASK) == 0 && (val & _ACKA_MASK) == _ACKA_MASK) {
						// /ACK  __--
						if (InteAout == true && (OutLatchC & _OBFA_MASK) == _OBFA_MASK)
							OutLatchC |= INTRA_MASK;
						else
							OutLatchC &= ~INTRA_MASK;
					}
				}
			}

			InBufferC = val;
			NotifyOnWritePortC(oldVal, OutLatchC);
			break;

		default :
//			warning("ChipPIO8255", "PeripheralWriteByte > invalid PIO port: %d", dest);
        		break;
	}
}
//---------------------------------------------------------------------------
public void PeripheralChangeBit(int dest, int bit, boolean state)
{
	int val;

	switch (dest) {
		case PP_PortA :
			InBufferA &= (int)(~(1 << bit));
			InBufferA |= (int)((state == true) ? (1 << bit) : 0);
			break;

		case PP_PortB :
			InBufferB &= (int)(~(1 << bit));
			InBufferB |= (int)((state == true) ? (1 << bit) : 0);
			break;

		case PP_PortC :
			if ((CWR & GB_MODE) == GB_MODE1 && (bit == _STBB || bit == _ACKB)) {
				if (bit == _STBB)
					val = (int)((InBufferC & ~_STBB_MASK) | ((state == true) ? _STBB_MASK : 0));
				else
					val = (int)((InBufferC & ~_ACKB_MASK) | ((state == true) ? _ACKB_MASK : 0));
				PeripheralWriteByte(dest, val);
			}
			else if ((CWR & GA_MODE) != GA_MODE0 && (bit == _STBA || bit == _ACKA)) {
				if (bit == _STBA)
					val = (int)((InBufferC & ~_STBA_MASK) | ((state == true) ? _STBA_MASK : 0));
				else
					val = (int)((InBufferC & ~_ACKA_MASK) | ((state == true) ? _ACKA_MASK : 0));
				PeripheralWriteByte(dest, val);
			}
			else {
				InBufferC &= (int)(~(1 << bit));
				InBufferC |= (int)((state == true) ? (1 << bit) : 0);
			}
			break;

		default :
//			warning("ChipPIO8255", "PeripheralChangeBit > invalid PIO port: %d", dest);
			break;
	}
}
//---------------------------------------------------------------------------
public int PeripheralReadByte(int src)
{
	int ret;

	switch (src) {
		case PP_PortA :
			if ((CWR & GA_MODE) == GA_MODE2) {
				if ((InBufferC & _ACKA_MASK) == 0)
					ret = OutLatchA;
				else if ((InBufferC & _STBA_MASK) == 0)
					ret = InBufferA;
				else
					ret = 0xFF;
			}
			else {
				if ((CWR & PORTA_DIR) == PORTA_OUT)
					ret = OutLatchA;
				else
					ret = InBufferA;
			}
			break;

		case PP_PortB :
			if ((CWR & PORTB_DIR) == PORTB_OUT)
				ret = OutLatchB;
			else
				ret = InBufferB;
			break;

		case PP_PortC :
			ret = 0;
			if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT))
				ret |= (int)(OutLatchC & 0x07);
			else if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_INP))
				ret |= (int)(InBufferC & 0x07);
			else if ((CWR & GB_MODE) == GB_MODE1) {
				ret |= (int)(OutLatchC & 0x03);
				ret |= (int)(InBufferC & 0x04);
			}

			if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_OUT))
				ret |= (int)(OutLatchC & 0xF0);
			else if ((CWR & (GA_MODE | PORTCH_DIR)) == (GA_MODE0 | PORTCH_INP))
				ret |= (int)(InBufferC & 0xF0);
			else if ((CWR & (GA_MODE | PORTA_DIR)) == (GA_MODE1 | PORTA_OUT)) {
				ret |= (int)(OutLatchC & 0x88);
				ret |= (int)(InBufferC & 0x70);
			}
			else if ((CWR & (GA_MODE | PORTA_DIR)) == (GA_MODE1 | PORTA_INP)) {
				ret |= (int)(OutLatchC & 0x28);
				ret |= (int)(InBufferC & 0xD0);
			}
			else if ((CWR & GA_MODE) == GA_MODE2) {
				ret |= (int)(OutLatchC & 0xA8);
				ret |= (int)(InBufferC & 0x50);
			}

			if ((CWR & GA_MODE) == GA_MODE0) {
				if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_OUT))
					ret |= (int)(OutLatchC & 0x08);
				else if ((CWR & (GB_MODE | PORTCL_DIR)) == (GB_MODE0 | PORTCL_INP))
					ret |= (int)(InBufferC & 0x08);
			}
			break;

		default :
//			warning("ChipPIO8255", "PeripheralReadByte > invalid PIO port: %d", src);
			ret = 0xFF;
			break;
	}

	return ret;
}
//---------------------------------------------------------------------------
public boolean PeripheralReadBit(int src, int bit)
{
	boolean ret;

	switch (src) {
		case PP_PortA :
			ret = ((PeripheralReadByte(PP_PortA) & (1 << bit))!=0);
			break;

		case PP_PortB :
			ret = ((PeripheralReadByte(PP_PortB) & (1 << bit))!=0);
			break;

		case PP_PortC :
			ret = ((PeripheralReadByte(PP_PortC) & (1 << bit))!=0);
			break;

		default :
//			warning("ChipPIO8255", "PeripheralReadBit > invalid PIO port: %d", src);
			ret = false;
			break;
	}

	return ret;
}
//---------------------------------------------------------------------------

}
