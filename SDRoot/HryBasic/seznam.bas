 
10 REM  Seznam programu
15 CLEAR500
16 DIM A$(30)
20 CLS:PRINT:PRINT:PRINT
30 PRINT"Novy list? ";:N=1:GOSUB500
40 IFV$<>"A"THEN150
50 POINTA0,0:MOVA150,100
60 SIZE0,33,-70,10
70 WRITE"SEZNAM PROGRAM"+CHR$(30)+"U NA KAZET"+CHR$(26)+"E :"
80 PRINT"Nazev kazety:";:N=1:GOSUB500
90 WRITEV$
100 MOVA300,900:WRITE"STRANA :"
110 PRINT"STRANA:";:N=1:GOSUB500
120 WRITEV$
130 MOVA400,100
150 PRINT"Kolik programu je na liste?";:N=2:GOSUB500
160 K=VAL(V$):P=K
170 P=P+1:CLS:PRINT&5,5"PROGRAM c.";P
175 IFP=27THEN200
180 PRINT"STAV POCITADLA:";:N=3:GOSUB500
185 A$(P)=V$+"  "
190 PRINT"NAZEV PROGRAMU:";:N=15:GOSUB500
195 A$(P)=A$(P)+V$+"  "
196 PRINT"POZNAMKA:";:N=14:GOSUB500
197 A$(P)=A$(P)+V$
198 PRINT"POKRACOVAT?";:N=1:GOSUB500
199 IFV$="A"THEN170
200 FOR I=KTOP
201 PRINTI;" ";A$(I)
202 NEXT I
203 PRINT"SOUHLASI TO?";:N=1:GOSUB500:O=1
204 IFV$="N"THENGOSUB800
205 IFO=0THEN200
206 VYS=50
207 SIZE0,34,-VYS,0
209 FORI=KTOP
210 POL=VYS*1.6*I+300
215 IF POL>2500THENMOVA0,0:NS=I:GOTO900
220 MOVAPOL,100:WRITEA$(I)
230 NEXTI
240 MOVA0,0:END
500 REM    PPGM:VSTUP
501 REM  INpromenna..N-delka
502 REM OUTpromenna..V$-retezec
503 REM Rozsah...500-750
504 REM Ostatni promenne:
505 REM          J,H,X,IN,IN$
510 GOSUB700
515 FOR J=1TON
516 PRINT".";
517 NEXT J
518 FOR J=1TON
519 PRINTCHR$(8);
520 NEXT J
525 J=0:V$=""
530 J=J+1
535 IN=USR(HEX(6000)):IN$=CHR$(IN)
537 POKE7,0
540 IFIN=0THEN535
550 IFIN=8THENJ=J-2
555 IFIN=13THENPRINT:PRINT"?:";:GOTO500
560 V$=V$+IN$:PRINTIN$;CHR$(7);
570 IFIN$<>""THEN570
580 IF J<NTHEN530
590 PRINT
600 RETURN
700 RESTORE740
710 FOR H=24576TO24580
720 READ X:POKEH,X
730 NEXT H
740 DATA205,201,248,121,201
750 RETURN
800 PRINT"KTERY RADEK?";:N@Ii;OSUB500
810 I=VAL(V$):CLS:PRINT&5,5"PROGRAM c.";I
820 PRINT"STAV POCITADLA:";:N=3:GOSUB500
830 A$(I)=V$+"  "
840 PRINT"NAZEV PROGRAMU:";:N=15:GOSUB500
850 A$(I)=A$(I)+V$+"  "
860 PRINT"POZNAMKA:";:N=14:GOSUB500
870 A$(I)=A$(I)+V$
880 O=0
890 RETURN
900 PRINT"NOVY PAPIR"
910 IFUSR(HEX(6000))=0THEN910
920 K=NS:GOTO207
