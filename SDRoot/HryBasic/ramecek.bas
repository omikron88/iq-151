0 REM RAMECEK PRO TECH. ZPRAVU2
2 L=L+1
10 MOVA0,0
20 VECTA2500,0
30 VECTA2500,1750
50 VECTA0,1750
60 VECTA0,0
69 IF Z=1THEN 110
70 MOVA0,350
80 VECTA2500,350
90 MOVA2500,1450
100 VECTA0,1450
110 MOVA0,0
111 FOR P=0TOL:CALL HEX(F973):NEXT P
120 PRINT"LIST CISLO ";L
130 PRINT"DALSI?   0-NE  1-ANO  2-VYPOCT."
140 INPUT Z
150 IF Z=1ORZ=2THEN2
160 MOVA0,0
170 END

