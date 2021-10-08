/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package machine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Io8272 {

    int RegSatus, ls1, ls2;
    int[] RegDat = new int[3];
    int[] Commands = new int[16];
    byte[] RWData = new byte[5000];
    int[] Outputs = new int[16];
    int ptrcommand, maxcommand, ptrdata, maxdata, ptrres, maxres, sisres;
    boolean b_execmode, b_command;
    int d1c,d2c; // barva diskety1,2
    boolean d1z,d2z; // priznak pro prekreslení   
    Iq m=null;
    public boolean dis2mnt=false;

    Io8272(Iq inM) {
        m = inM;
    }
    
    public void I8272reset() {
        Arrays.fill(RegDat, 0);
        Arrays.fill(Commands, 0);
        Arrays.fill(RWData, (byte) 0);
        ptrcommand = 0;
        ptrdata = 0;
        maxdata = 0;
        maxcommand = 0;
        ls1 = 32;
        ls2 = 0;
        b_execmode = false;
        b_command = false;
        sisres = 1;
        RegSatus = 128;
    }

    public int I8272in(int port) {
        int nRet;
        if (port == 0xAA) {
            nRet = RegSatus;
        } else {
            if (b_execmode) {
                nRet = 0xff & RWData[ptrdata];
                ptrdata++;
                if (ptrdata == maxdata) {
                    //System.out.println("IN bajtu predano=" + maxdata);
                    if ((Commands[2] & 3) == 1) {
                        d1c = 3;
                        d1z = true;
                    } else {
                        d2c = 3;
                        d2z = true;
                    }
                    b_execmode = false;
                    ptrres = 1;
                    Outputs[1] = Commands[2] & 7;
                    Outputs[2] = 0;
                    Outputs[3] = 0;
                    Outputs[4] = Commands[3];
                    Outputs[5] = Commands[4];
                    Outputs[6] = Commands[7];
                    Outputs[7] = Commands[6];
                    RegSatus = 208;    // 1101 0000 bylo 176 1011 0000
                }
            }else{
              nRet=Outputs[ptrres];
              ptrres++;
              if (ptrres>maxres){
                  //System.out.println("IN "+maxres +" bajtu vysledku predano");
                  RegSatus=128;
              }
            }
        }
        return nRet;
    }

    public void I8272out(int port,byte data){
        int h,i;
        if((RegSatus & 64)==0){
            if(b_execmode){
              //zapis do radice - na disketu
              RWData[ptrdata]=data;
              ptrdata++;
              if(ptrdata==maxdata){
                  if ((Commands[2] & 3) == 1){
                      h=Commands[3] * m.floppyA.tracksize+(Commands[5]-1)*128;                      
                  }else{ 
                      h=Commands[3]* m.floppyB.tracksize+(Commands[5]-1)*128;                   
                  }
                  for(i=0;i<maxdata;i++){
                      if((Commands[2] & 3) == 1){
                         m.floppyA.dimage[h+i]=RWData[i]; 
                      }else{
                         m.floppyB.dimage[h+i]=RWData[i]; 
                      }
                  }
                  if ((Commands[2] & 3) == 1) {
                      if ((utils.Config.bFlop1RW)&&(utils.Config.bFlop1Inserted)) {                                                         
                          try {
                              FileOutputStream fos = new FileOutputStream(utils.Config.strFlop1FilePath);
                              fos.write(m.floppyA.dimage);
                              fos.close();
                          } catch (IOException ex) {
                          }                              
                      }
                  } else {
                      if ((utils.Config.bFlop2RW)&&(utils.Config.bFlop2Inserted)) { 
                        try {
                              FileOutputStream fos = new FileOutputStream(utils.Config.strFlop2FilePath);
                              fos.write(m.floppyB.dimage);
                              fos.close();
                          } catch (IOException ex) {
                          }  
                      }
                  }
                  b_execmode=false;
                  //System.out.println("OUT bajtu dat prijato="+maxdata);
                  if ((Commands[2] & 3) == 1) {
                      d1c = 3;
                      d1z = true;
                  } else {
                      d2c = 3;
                      d2z = true;
                    }
                    ptrres = 1;
                    Outputs[1] = Commands[2] & 7;
                    Outputs[2] = 0;
                    Outputs[3] = 0;
                    Outputs[4] = Commands[3];
                    Outputs[5] = Commands[4];
                    Outputs[6] = Commands[7];
                    Outputs[7] = Commands[6];
                    RegSatus = 208;
                }
            }else{
              // zapis commandu
              if(b_command){
                 Commands[ptrcommand]=0xff & data;
                 ptrcommand++;
                if(ptrcommand>maxcommand) executeCommand();
              }else{
                Commands[1]=0xff & (data & 15);
                ptrcommand=2;
                b_command=true;
                switch(Commands[1]){
                    case 0:
                        maxcommand=1;
                        executeCommand();
                        break;
                    case 3:
                        maxcommand=3;
                        break;
                    case 5:
                        maxcommand=9;
                        break;
                    case 6:
                        maxcommand=9;
                        break;
                    case 7:
                        maxcommand=2;
                        break;
                    case 8:
                        maxcommand=1;
                        executeCommand();
                        break;
                    case 15:
                            maxcommand=3;
                    default:
                            break;
                    }
                }
            }
        }
    }
 
    void executeCommand(){
        int i,h;
        String strDebug = "";
        for (i = 1; i <= maxcommand; i++) {
            strDebug += Commands[i] + " ";
        }
        //System.out.print(strDebug);
        b_command=false;
        maxres=0;
        ptrres=1;
        switch (Commands[1]) {
            case 0:
                maxres = 1;
                Outputs[1] = 128;
                sisres = 1;
                break;
            case 3:
                b_execmode = false;
                sisres = 1;
                RegSatus = 128;
                break;
            case 5:
                maxres = 7;
                sisres = 1;
                if (Commands[6] == 0) {
                    h = Commands[9];
                } else {
                    h = Commands[6];
                }
                if (h != 128) {
                    //System.out.println("neodpovida delka dat sektoru diskety =128 / " + h);
                }
                maxdata = (Commands[7] - Commands[5] + 1) * h;
                ptrdata = 0;
                RegSatus = 176; // 1011 0000
                b_execmode = true;
                if ((Commands[2] & 3) == 1) {
                    d1c = 5;
                    d1z = true;
                } else {
                    d2c = 5;
                    d2z = true;
                }
                //System.out.println("k zápisu bajtů: " + maxdata + " st:" + Commands[3] + " sec:" + Commands[5]);
                break;
            case 6:
                maxres = 7;
                sisres = 1;
                if (Commands[6] == 0) {
                    h = Commands[9];
                } else {
                    h = Commands[6];
                }
                maxdata = (Commands[7] - Commands[5] + 1) * h;
                ptrdata = 0;
                if (h != 128) {
                    //System.out.println("neodpovida delka dat sektoru diskety =128 / " + h);
                }
                if ((Commands[2] & 3) == 1) {
                    h = Commands[3] * m.floppyA.tracksize + (Commands[5] - 1) * 128;
                } else {
                    h = Commands[3] * m.floppyB.tracksize + (Commands[5] - 1) * 128;
                }
                for (i = 0; i < maxdata; i++) {
                    if ((Commands[2] & 3) == 1) {
                        RWData[i] = m.floppyA.dimage[h + i];
                    } else {
                        RWData[i] = m.floppyB.dimage[h + i];
                    }
                }
                RegSatus = 240; // 1111 0000                
                b_execmode = true;
                if ((Commands[2] & 3) == 1) {
                    d1c = 4;
                    d1z = true;
                } else {
                    d2c = 4;
                    d2z = true;
                }
                //System.out.println("nachystáno bajtů: " + maxdata + " st:" + Commands[3] + " sec:" + Commands[5]);
                break;
            case 7:
                ls1 = 0;
                ls2 = 0;
                sisres = 2;
                break;
            case 8:
                maxres = sisres;
                if (maxres == 1) {
                    Outputs[1] =  128;
                } else {
                    Outputs[1] =  ls1;
                }
                Outputs[2] =  ls2;
                sisres = 1;
                b_execmode = false;
                RegSatus = 192; // 1100 0000
                break;
            case 15:
                ls1 = 32 + (Commands[2] & 3);  // nastav hodnoty last seek pro 8
                ls2 = Commands[2];
                sisres = 2;
                break;
            default:
                break;
        }

    }
}
