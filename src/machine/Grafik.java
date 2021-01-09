/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;


/**
 *
 * @author Luke
 */
public class Grafik {
    public byte[] GVRam = new byte[16384]; 
    public boolean Enabled;
    public boolean ShowGR;
    public boolean BitAcces;
    public boolean PenOn;
    public int D0;
    public int D1;
    
    private byte[][] GrafRam = new byte[64][256];
    private byte[] zrct = new byte[256];
    private static final char gbt0[] = {1,2,4,8,16,32,64,128};
    private static final char gbt1[] = {254,253,251,247,239,223,191,127};
    private int[][] gvradr = new int[64][256];
    
    public void Init() {
       for(int j=0; j<256; j++) {zrct[j]=zrcadlo(j);}
       for(int i=0; i<64; i++) {
         for(int j=0; j<256; j++) {gvradr[i][j]=i+16320-j*64;}
       }  
       BitAcces=false;
       PenOn=false;
       ShowGR=true;
       Enabled=true;
    }       
    
    public void wpD2(int value) {
         if ((value & 1)==1) { BitAcces= false;} else{ BitAcces=true;}
         if ((value & 8)==8) { ShowGR= true;} else{ ShowGR=false;}
         if ((value & 2)==2) { PenOn= true;} else{ PenOn=false;}
    }
    public void wpD3(int value) {

    }
    
    public void wpD4(int value) {
        int i = D0&63;
        int j = D1& 0xff;
        if (BitAcces) {
         int a=GrafRam[i][j]& 0xff;
         int c=(value>>5) & 7;
         if (PenOn) {a&=gbt1[c] ;} 
         else {a|=gbt0[c] ;}
         GrafRam[i][j] = (byte)a;
         GVRam[gvradr[i][j]] = zrct[GrafRam[i][j]&255];
        } 
        else {GrafRam[i][j] = (byte)value;
              GVRam[gvradr[i][j]] = zrct[GrafRam[i][j]&255];}
     }  
    
    public int rpD4() {
        int i = D0&63;
        int j = D1& 0xff;
        return GrafRam[i][j]& 0xff;
    }
    
    private byte zrcadlo(int xx) {
        int pom = 0;
        if ((xx & 1)==1) { pom|= 128;}
        if ((xx & 2)==2) { pom|= 64;}
        if ((xx & 4)==4) { pom|= 32;}
        if ((xx & 8)==8) { pom|= 16;}
        if ((xx & 16)==16) { pom|= 8;}
        if ((xx & 32)==32) { pom|= 4;}
        if ((xx & 64)==64) { pom|= 2;}
        if ((xx & 128)==128) { pom|= 1;}
        return (byte) pom;
    } 
    
    
}
