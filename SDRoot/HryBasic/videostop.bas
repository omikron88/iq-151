 
10 REM***Program :B3 VIDEOSTOP***
20 REM***Pocitac :IQ 151***
30 REM***Autor   :MILOS VESELY***
40 CLS:PRINT:PRINT:PRINT"Vasim ukolem je zastavovat"
50 PRINT"stejna cisla kostek."
60 PRINT"Hru ovladate klavesou K."
65 PRINT&12,5"1-nejlehci":PRINT&10,0"";
70 INPUT"VOLTE OBTIZNOST  1-5";P
80 IF P<1 OR P>5 THEN PRINT"ALE TO NE!":GOTO70:REM**NOVY VSTUP**
90 LET P=7-P
100 REM***KRESBA HERNIHO PLANU***
110 CLS
120 FORI=3TO26
130 PRINTCHR$(15)&3,I"Q";&26,I"Q";&5,I"Q";
140 PRINT&I,3"T";&I,26"T";
150 NEXT I
160 PRINT&3,3"P";&3,26"_";&5,3"S";&5,26"N";&26,3"K";&26,26"J"
170 PRINT&4,10"VIDEOSTOP"
180 PRINT&7,4"BODY:      POKUSY:"
190 PRINTCHR$(15)&10,8"PQQQRQQQRQQQ_";
200 PRINT&11,8"TMMMTMMMTMMMT";
210 PRINT&12,8"TMMMTMMMTMMMT";
220 PRINT&13,8"TMMMTMMMTMMMT";
230 PRINT&14,8"KQQQLQQQLQQQJ"
240 REM***HRA***
242 PK=10
245 IFINKEY$=""THEN245
248 IFINKEY$<>""THEN248
250 A=INT(6*RND(0)+1)
260 B=INT(6*RND(0)+1)
270 C=INT(6*RND(0)+1)
275 POKE23,77:POKE24,2
280 PRINT&12,9 A;&12,13 B;&12,17 C;CHR$(15)&12,20"T"
290 PRINTCHR$(7)
295 WAIT(P)
300 IFINKEY$="K"THEN320
310 GOTO250:REM**NOVA CISLA"
320 PK=PK-1
325 POKE24,18:POKE23,103
330 IF A=B AND A=CTHEN BO=BO+100:PRINT&20,8"ZISK 100 BODU":POKE23,6:GOTO350
340 IF A=B OR A=C OR B=C THENBO=BO+50:PRINT&20,8"ZISK 50 BODU":POKE23,10
350 PRINT&7,10 BO;&7,22 PK;CHR$(7)
355 IF NOT INKEY$=""THEN355
360 IF INKEY$=""THEN360
370 IF PK=0THEN390:REM**KONEC**
380 PRINT&20,8"              ":GOTO250:REM**NOVY POKUS**
390 PRINT&20,8"  KONEC     "
395 IF NOT INKEY$=""THEN395
400 PRINTTAB(6);"Jeste?"
405 IFINKEY$=""THEN405
406 IFINKEY$="A"THENRUN
410 CLS:END
