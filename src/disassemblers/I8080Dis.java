//Author: tmilata
//original c++ code by Jayachandra Kasarla
//GitHub: https://www.github.com/jaychandra6
//LICENSE: MIT
package disassemblers;

public class I8080Dis {

    public static int[] Opcodes;
    public static byte nInstrLen;

    public String Disassemble(int adr) {
        String s;

        int a = Opcodes[adr];

        //opbytes will tell us by how much the program counter should be
        //incremented in order to fetch the correct instruction

        byte opbytes = 1;

        //following switch block will check every byte from the ROM and prints it according 
        //to the pre-defined instructions.
        switch (a) {
            case 0x00:
            case 0x08:
            case 0x10:
            case 0x18:
            case 0x28:
            case 0x38:
            case 0xcb:
            case 0xd9:
            case 0xdd:
            case 0xed:
            case 0xfd:
                s = String.format("NOP");
                break;
            case 0x01:
                //LXI performs operation on two bytes,so we print next two bytes of the buffer
                //and assign opbytes = 3
                //LXI is performed on B register
                s = String.format("LXI B,#%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x02:
                s = String.format("STAX B");
                break;
            case 0x03:
                s = String.format("INX B");
                break;
            case 0x04:
                s = String.format("INR B");
                break;
            case 0x05:
                s = String.format("DCR B");
                break;
            case 0x06:
                //MVI Instruction performs operation on next single byte
                //So,we fetch the next byte from the buffer using Opcodes[adr+1]
                //and return opbytes = 2
                s = String.format("MVI B,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x07:
                s = String.format("RLC");
                break;
            case 0x09:
                s = String.format("DAD B");
                break;
            case 0x0a:
                s = String.format("LDAX B");
                break;
            case 0x0b:
                s = String.format("DCX B");
                break;
            case 0x0c:
                s = String.format("INR C");
                break;
            case 0x0d:
                s = String.format("DCR C");
                break;
            case 0x0e:
                s = String.format("MVI C,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x0f:
                s = String.format("RRC");
                break;
            case 0x11:
                s = String.format("LXI D,#%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x12:
                s = String.format("STAX D");
                break;
            case 0x13:
                s = String.format("INX D");
                break;
            case 0x14:
                s = String.format("INR D");
                break;
            case 0x15:
                s = String.format("DCR D");
                break;
            case 0x16:
                s = String.format("DCR D");
                break;
            case 0x17:
                s = String.format("RAL");
                break;
            case 0x19:
                s = String.format("DAD D");
                break;
            case 0x1a:
                s = String.format("LDAX D");
                break;
            case 0x1b:
                s = String.format("DCX D");
                break;
            case 0x1c:
                s = String.format("INR E");
                break;
            case 0x1d:
                s = String.format("DCR E");
                break;
            case 0x1e:
                s = String.format("MVI E,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x1f:
                s = String.format("RAR");
                break;
            case 0x20:
                s = String.format("RIM");
                break;
            case 0x21:
                s = String.format("LXI H,#%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x22:
                s = String.format("SHLD #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x23:
                s = String.format("INX H");
                break;
            case 0x24:
                s = String.format("INR H");
                break;
            case 0x25:
                s = String.format("DCR H");
                break;
            case 0x26:
                s = String.format("MVI H,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x27:
                s = String.format("DAA");
                break;
            case 0x29:
                s = String.format("DAD H");
                break;
            case 0x2a:
                s = String.format("LHLD #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x2b:
                s = String.format("DCX H");
                break;
            case 0x2c:
                s = String.format("INR L");
                break;
            case 0x2d:
                s = String.format("DCR L");
                break;
            case 0x2e:
                s = String.format("MVI L,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x2f:
                s = String.format("CMA");
                break;
            case 0x30:
                s = String.format("SIM");
                break;
            case 0x31:
                s = String.format("LXI SP,#%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x32:
                s = String.format("STA #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x33:
                s = String.format("INX SP");
                break;
            case 0x34:
                s = String.format("INR M");
                break;
            case 0x35:
                s = String.format("DCR M");
                break;
            case 0x36:
                s = String.format("MVI M,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x37:
                s = String.format("STC");
                break;
            case 0x39:
                s = String.format("DAD SP");
                break;
            case 0x3a:
                s = String.format("LDA #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0x3b:
                s = String.format("DCX SP");
                break;
            case 0x3c:
                s = String.format("INR A");
                break;
            case 0x3d:
                s = String.format("DCR A");
                break;
            case 0x3e:
                s = String.format("MVI A,#%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0x3f:
                s = String.format("CMC");
                break;
            case 0x40:
                s = String.format("MOV B,B");
                break;
            case 0x41:
                s = String.format("MOV B,C");
                break;
            case 0x42:
                s = String.format("MOV B,D");
                break;
            case 0x43:
                s = String.format("MOV B,E");
                break;
            case 0x44:
                s = String.format("MOV B,H");
                break;
            case 0x45:
                s = String.format("MOV B,L");
                break;
            case 0x46:
                s = String.format("MOV B,M");
                break;
            case 0x47:
                s = String.format("MOV B,A");
                break;
            case 0x48:
                s = String.format("MOV C,B");
                break;
            case 0x49:
                s = String.format("MOV C,C");
                break;
            case 0x4a:
                s = String.format("MOV C,D");
                break;
            case 0x4b:
                s = String.format("MOV C,E");
                break;
            case 0x4c:
                s = String.format("MOV C,H");
                break;
            case 0x4d:
                s = String.format("MOV C,L");
                break;
            case 0x4e:
                s = String.format("MOV C,M");
                break;
            case 0x4f:
                s = String.format("MOV C,A");
                break;
            case 0x50:
                s = String.format("MOV D,B");
                break;
            case 0x51:
                s = String.format("MOV D,C");
                break;
            case 0x52:
                s = String.format("MOV D,D");
                break;
            case 0x53:
                s = String.format("MOV D,E");
                break;
            case 0x54:
                s = String.format("MOV D,H");
                break;
            case 0x55:
                s = String.format("MOV D,L");
                break;
            case 0x56:
                s = String.format("MOV D,M");
                break;
            case 0x57:
                s = String.format("MOV D,A");
                break;
            case 0x58:
                s = String.format("MOV E,B");
                break;
            case 0x59:
                s = String.format("MOV E,C");
                break;
            case 0x5a:
                s = String.format("MOV E,D");
                break;
            case 0x5b:
                s = String.format("MOV E,E");
                break;
            case 0x5c:
                s = String.format("MOV E,H");
                break;
            case 0x5d:
                s = String.format("MOV E,L");
                break;
            case 0x5e:
                s = String.format("MOV E,M");
                break;
            case 0x5f:
                s = String.format("MOV E,A");
                break;
            case 0x60:
                s = String.format("MOV H,B");
                break;
            case 0x61:
                s = String.format("MOV H,C");
                break;
            case 0x62:
                s = String.format("MOV H,D");
                break;
            case 0x63:
                s = String.format("MOV H,E");
                break;
            case 0x64:
                s = String.format("MOV H,H");
                break;
            case 0x65:
                s = String.format("MOV H,L");
                break;
            case 0x66:
                s = String.format("MOV H,M");
                break;
            case 0x67:
                s = String.format("MOV H,A");
                break;
            case 0x68:
                s = String.format("MOV L,B");
                break;
            case 0x69:
                s = String.format("MOV L,C");
                break;
            case 0x6a:
                s = String.format("MOV L,D");
                break;
            case 0x6b:
                s = String.format("MOV L,E");
                break;
            case 0x6c:
                s = String.format("MOV L,H");
                break;
            case 0x6d:
                s = String.format("MOV L,L");
                break;
            case 0x6e:
                s = String.format("MOV L,M");
                break;
            case 0x6f:
                s = String.format("MOV L,A");
                break;
            case 0x70:
                s = String.format("MOV M,B");
                break;
            case 0x71:
                s = String.format("MOV M,C");
                break;
            case 0x72:
                s = String.format("MOV M,D");
                break;
            case 0x73:
                s = String.format("MOV M,E");
                break;
            case 0x74:
                s = String.format("MOV M,H");
                break;
            case 0x75:
                s = String.format("MOV M,L");
                break;
            case 0x76:
                s = String.format("HLT");
                break;
            case 0x77:
                s = String.format("MOV M,A");
                break;
            case 0x78:
                s = String.format("MOV A,B");
                break;
            case 0x79:
                s = String.format("MOV A,C");
                break;
            case 0x7a:
                s = String.format("MOV A,D");
                break;
            case 0x7b:
                s = String.format("MOV A,E");
                break;
            case 0x7c:
                s = String.format("MOV A,H");
                break;
            case 0x7d:
                s = String.format("MOV A,L");
                break;
            case 0x7e:
                s = String.format("MOV A,M");
                break;
            case 0x7f:
                s = String.format("MOV A,A");
                break;
            case 0x80:
                s = String.format("ADD B");
                break;
            case 0x81:
                s = String.format("ADD C");
                break;
            case 0x82:
                s = String.format("ADD D");
                break;
            case 0x83:
                s = String.format("ADD E");
                break;
            case 0x84:
                s = String.format("ADD H");
                break;
            case 0x85:
                s = String.format("ADD L");
                break;
            case 0x86:
                s = String.format("ADD M");
                break;
            case 0x87:
                s = String.format("ADD A");
                break;
            case 0x88:
                s = String.format("ADC B");
                break;
            case 0x89:
                s = String.format("ADC C");
                break;
            case 0x8a:
                s = String.format("ADC E");
                break;
            case 0x8b:
                s = String.format("ADC E");
                break;
            case 0x8c:
                s = String.format("ADC H");
                break;
            case 0x8d:
                s = String.format("ADC L");
                break;
            case 0x8e:
                s = String.format("ADC M");
                break;
            case 0x8f:
                s = String.format("ADC A");
                break;
            case 0x90:
                s = String.format("SUB B");
                break;
            case 0x91:
                s = String.format("SUB C");
                break;
            case 0x92:
                s = String.format("SUB D");
                break;
            case 0x93:
                s = String.format("SUB E");
                break;
            case 0x94:
                s = String.format("SUB H");
                break;
            case 0x95:
                s = String.format("SUB L");
                break;
            case 0x96:
                s = String.format("SUB M");
                break;
            case 0x97:
                s = String.format("SUB A");
                break;
            case 0x98:
                s = String.format("SBB B");
                break;
            case 0x99:
                s = String.format("SBB C");
                break;
            case 0x9a:
                s = String.format("SBB D");
                break;
            case 0x9b:
                s = String.format("SBB E");
                break;
            case 0x9c:
                s = String.format("SBB H");
                break;
            case 0x9d:
                s = String.format("SBB L");
                break;
            case 0x9e:
                s = String.format("SBB M");
                break;
            case 0x9f:
                s = String.format("SBB A");
                break;
            case 0xa0:
                s = String.format("ANA B");
                break;
            case 0xa1:
                s = String.format("ANA C");
                break;
            case 0xa2:
                s = String.format("ANA D");
                break;
            case 0xa3:
                s = String.format("ANA E");
                break;
            case 0xa4:
                s = String.format("ANA H");
                break;
            case 0xa5:
                s = String.format("ANA L");
                break;
            case 0xa6:
                s = String.format("ANA M");
                break;
            case 0xa7:
                s = String.format("ANA A");
                break;
            case 0xa8:
                s = String.format("XRA B");
                break;
            case 0xa9:
                s = String.format("XRA C");
                break;
            case 0xaa:
                s = String.format("XRA D");
                break;
            case 0xab:
                s = String.format("XRA E");
                break;
            case 0xac:
                s = String.format("XRA H");
                break;
            case 0xad:
                s = String.format("XRA L");
                break;
            case 0xae:
                s = String.format("XRA M");
                break;
            case 0xaf:
                s = String.format("XRA A");
                break;
            case 0xb0:
                s = String.format("ORA B");
                break;
            case 0xb1:
                s = String.format("ORA C");
                break;
            case 0xb2:
                s = String.format("ORA D");
                break;
            case 0xb3:
                s = String.format("ORA E");
                break;
            case 0xb4:
                s = String.format("ORA H");
                break;
            case 0xb5:
                s = String.format("ORA L");
                break;
            case 0xb6:
                s = String.format("ORA M");
                break;
            case 0xb7:
                s = String.format("ORA A");
                break;
            case 0xb8:
                s = String.format("CMP B");
                break;
            case 0xb9:
                s = String.format("CMP C");
                break;
            case 0xba:
                s = String.format("CMP D");
                break;
            case 0xbb:
                s = String.format("CMP E");
                break;
            case 0xbc:
                s = String.format("CMP H");
                break;
            case 0xbd:
                s = String.format("CMP L");
                break;
            case 0xbe:
                s = String.format("CMP M");
                break;
            case 0xbf:
                s = String.format("CMP A");
                break;
            case 0xc0:
                s = String.format("RNZ");
                break;
            case 0xc1:
                s = String.format("POP B");
                break;
            case 0xc2:
                s = String.format("JNZ #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xc3:
                s = String.format("JMP #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xc4:
                s = String.format("CNZ #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xc5:
                s = String.format("PUSH B");
                break;
            case 0xc6:
                s = String.format("ADI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xc7:
                s = String.format("RST 0");
                break;
            case 0xc8:
                s = String.format("RZ");
                break;
            case 0xc9:
                s = String.format("RET");
                break;
            case 0xca:
                s = String.format("JZ #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xcc:
                s = String.format("CZ #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xcd:
                s = String.format("CALL #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xce:
                s = String.format("ACI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xcf:
                s = String.format("RST 1");
                break;
            case 0xd0:
                s = String.format("RNC");
                break;
            case 0xd1:
                s = String.format("POP D");
                break;
            case 0xd2:
                s = String.format("JNC #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xd3:
                s = String.format("OUT #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xd4:
                s = String.format("CNC #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xd5:
                s = String.format("PUSH D");
                break;
            case 0xd6:
                s = String.format("SUI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xd7:
                s = String.format("RST 2");
                break;
            case 0xd8:
                s = String.format("RC");
                break;
            case 0xda:
                s = String.format("JC #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xdb:
                s = String.format("IN #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xdc:
                s = String.format("CC #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xde:
                s = String.format("SBI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xdf:
                s = String.format("RST 3");
                break;
            case 0xe0:
                s = String.format("RPO");
                break;
            case 0xe1:
                s = String.format("POP H");
                break;
            case 0xe2:
                s = String.format("JPO #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xe3:
                s = String.format("XTHL");
                break;
            case 0xe4:
                s = String.format("CPO #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xe5:
                s = String.format("PUSH H");
                break;
            case 0xe6:
                s = String.format("ANI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xe7:
                s = String.format("RST 4");
                break;
            case 0xe8:
                s = String.format("RPE");
                break;
            case 0xe9:
                s = String.format("PCHL");
                break;
            case 0xea:
                s = String.format("JPE #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xeb:
                s = String.format("XCHG");
                break;
            case 0xec:
                s = String.format("CPE #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xee:
                s = String.format("XRI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xef:
                s = String.format("RST 5");
                break;
            case 0xf0:
                s = String.format("RP");
                break;
            case 0xf1:
                s = String.format("POP PSW");
                break;
            case 0xf2:
                s = String.format("JP #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xf3:
                s = String.format("DI");
                break;
            case 0xf4:
                s = String.format("CP #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xf5:
                s = String.format("PUSH PSW");
                break;
            case 0xf6:
                s = String.format("ORD #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xf7:
                s = String.format("RST 6");
                break;
            case 0xf8:
                s = String.format("RM");
                break;
            case 0xf9:
                s = String.format("SPHL");
                break;
            case 0xfa:
                s = String.format("JM #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xfb:
                s = String.format("EI");
                break;
            case 0xfc:
                s = String.format("CM #%04X", Opcodes[adr + 1] + (Opcodes[adr + 2] << 8));
                opbytes = 3;
                break;
            case 0xfe:
                s = String.format("CPI #%02X", Opcodes[adr + 1]);
                opbytes = 2;
                break;
            case 0xff:
                s = String.format("RST 7");
                break;

            default:
                // Unknown instruction
                s = String.format("Unknown Instruction: 0x%02x ", a);
                break;
        }
        //return the number of opbytes,so that we can increment the program counter correctly
        nInstrLen = opbytes;

        return s;
    }
}