2000 CLS
2010 LET A=155: LET K1=39
2020 LET W=75: LET K=0.65
2030 LET XNULA=128: LET YNULA=83: LET BM=3.1415/180
2040 LET C=K*COS (W*BM): LET S=K*SIN (W*BM)
2050 LET DX=3: LET DY=5: LET AF=A/90
2060 DIM H(256)
2061 SCALE 0,256,0,192
2070 FOR I=1 TO 256: LET H(I)=-1000: NEXT I
2080 FOR G=-110 TO 110 STEP DY: LET Y=G*AF
2090 FOR M=-105 TO 105 STEP DX
2100 LET X=M*AF: GOSUB 2400
2110 LET XA=INT (XNULA+M+C*G+0.5): LET YA=INT (YNULA+S*G+Z+0.5)
2130 IF M>-105 THEN GOTO 2170
2140 LET F1=0: LET I=INT (XA/DX)
2150 IF YA>=H(I+1) THEN LET F1=1: LET H(I+1)=YA
2160 LET X1=XA: LET Y1=YA: GOTO 2220
2170 LET F2=0: LET I=INT (XA/DX)
2180 IF YA>=H(I+1) THEN LET F2=1: LET H(I+1)=YA
2190 LET X2=XA: LET Y2=YA
2200 IF F1*F2=1 THEN DRAW X1,Y1,-2: DRAW X2,Y2,-1 
2210 LET X1=X2: LET Y1=Y2: LET F1=F2
2220 NEXT M
2230 NEXT G
2250 STOP
2400 LET R=SQR(X*X+Y*Y)*BM
2410 LET Z=K1*(COS (R) - COS (3*R)/3 + COS (5*R)/5 - COS (7*R)/7)+24
2420 RETURN 