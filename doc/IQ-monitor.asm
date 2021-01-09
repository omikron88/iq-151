;IQ 151 MONITOR
;==============
;
;Start je na adrese 0F800h, kter� je po startu IQ HW p�epnuta i na 0000h
;
;
;Syst�mov� prom�nn� Monitoru:
;============================
;0000-0002 - voln�, mo�no vyu��t p�i RST 0
;0003      - I/O bajt (69 standartn�)
;0004-0005 - konec RAM (7FFF pro 32kB)
;0006      - �asov�n� kl�vesnice a blik�n� kurzoru
;0007      - 0=kurzor blik�, jinak blokov�no
;0008-000A - ��ta� 20 ms; zastaveno p�i zamaskovan�m p�eru�en� 50 Hz 8259
;000B      - k�d znaku na kter� ukazuje kurzor
;000C-000D - adresa m�sta na obrazovce na kter� ukazuje kurzor ve Videoram
;000E      - sloupec, ve kter�m je pr�v� kurzor (00-1F - 00-3F pro Video 64)
;000F      - ��dek, ve kter�m je pr�v� kurzor
;0010      - 1=grafick� znaky, 0=norm�ln� re�im
;0011      - 1=znaky v inverzi, 0=norm�ln� re�im
;0012      - po�et posunovan�ch znak� v p��kazech DC a IL
;0013      - d�lka str�nky (01-1F)
;0014      - ��dkov�n� (o kolik dol� se posune kurzor p�i od��dkov�n� CR - znak 0D)
;0015-0016 - n�vratov� adresa monitoru pro p��kaz R
;0017-0018 - v��ka a d�lka t�nu pro rutinu F973
;0019-001A - adresa bufferu pro magnetofon
;001B      - bit 7 ur�uje polaritu sign�lu na p�sce (standartn� 0), ostatn� bity jsou ��slo
; 	     �m�rn� d�lce periody modulovan�ho kmito�tu (1kHz)
;001C      - �as pro �ekac� smy�ku F5A2 (mezi bloky na p�sce)
;001D-001E - adresa tepl�ho startu (Basicu p��padn� jin�ho jazyka podle p�ipojen�ho modulu)
;001F      - po�et znak� na ��dek - 20/40h Video32/64
;0020-0021 - adresa za��tku VIDEORAM EC00 pro Video32, E800 pro Video64
;0022-002D - adresa, do kter� se ukl�daj� hodnoty registr� procesoru
;          22 - F
;          23 - A
;          24 - C
;          25 - B
;          26 - E
;          27 - D
;          28 - L
;          29 - H
;          2A+2B - SP
;          2C+2D - PC
;


;Komentovan� v�pis:
===================


;znak z kl�vesnice na obrazovku (I/O=69)
;nebo na periferii podle I/O bajtu (F64B)
;----------------------------------------
f000 cd2bf6    call    0f62bh		;vyzvedni znak z kl�vesnice (periferie)

;ASCII znak na periferii podle I/O bajtu
;podle rutiny F6B4, vstup v reg. A
;---------------------------------------
f003 4f        ld      c,a		;dej znak do C
f004 c34bf6    jp      0f64bh		;tisk podle I/O bajtu (podle F64B)


;znak v ASCII k�du z reg. C obrazovku
;v�etn� ��d�c�ch znak�
-------------------------------------
f007 f5        push    af		;uschovej registry
f008 c5        push    bc
f009 d5        push    de
f00a e5        push    hl
f00b 2190f1    ld      hl,0f190h	;0F190h je adresa p�es kterou se bude rutina	 							;vracet a kon�it
f00e e5        push    hl		;dej ji na z�sobn�k 
f00f 210700    ld      hl,0007h
f012 75        ld      (hl),l		;vynuluje obsah adresy 0007 - povol� blik�n� kurzoru
f013 3a0b00    ld      a,(000bh)	;vlo� do A k�d znaku na kter� ukazuje kurzor
f016 2a0c00    ld      hl,(000ch)	;do HL dej adresu kurzoru ve Videoram
f019 77        ld      (hl),a		;vlo� na ni ten znak
f01a 111000    ld      de,0010h
f01d 79        ld      a,c		;do A znak kter� bude rutina F007 tisknout
f01e fe20      cp      20h		;je to ��d�c� k�d (<32) nebo ne?
f020 f25bf1    jp      p,0f15bh		;tisk znaku s k�dem 32 a vy���m, jinak ��d�c� k�d 0-31
f023 d607      sub     07h		;nezpracov�vej k�dy 0-6
f025 d8        ret     c		;je to 0-6 tak�e n�vrat
f026 2139f0    ld      hl,0f039h	;tabulka s offsety ��d�c�ch k�d�
f029 85        add     a,l		;najdi odpov�daj�c� pozici v tabulce
f02a 6f        ld      l,a		;adresa je te� v HL
f02b d5        push    de		;uschovej DE
f02c 5e        ld      e,(hl)		;vyzvedni z tabulky offset adresy
f02d 1600      ld      d,00h		;je to jen 1 bajt (v�echny rutiny jsou v rozsahu
f02f 2152f0    ld      hl,0f052h	;255 bajt� od 0F052)
f032 19        add     hl,de		;vypo�ti adresu rutiny ��d�c�ho znaku 7-31
f033 d1        pop     de		;obnov DE
f034 e5        push    hl		;adresa rutiny na z�sobn�k
f035 210e00    ld      hl,000eh		;do HL 000Eh - adresa sloupce kurzoru - pro k�d 0D
f038 c9        ret     			;sko� na p��slu�nou rutinu

;tabulka s offsety ��d�c�ch k�d�
;-------------------------------
f039 1a        db      01Ah		;F06Ch => k�d 07 - p�pnut� (je na F052+1A)
f03a 1d        db      01Dh		;F06Fh => k�d 08 - kurzor vlevo
f03b 61        db      061h		;F0B3h => k�d 09 - tabul�tor 8 znak�
f03c 19        db      019h		;F06Bh => k�d 0A - pouze RET (ten nejbli�� :) )
f03d 19        db      019h		;F06Bh => k�d 0B - pouze RET
f03e 13        db      013h		;F065h => k�d 0C - kurzor na pozici 0,0 (HOME)
f03f e5        db      0E5h		;F137h => k�d 0D - od��dkov�n� a zru�en� grafick�ho
					;                  i inverzn�ho re�imu (CR)
f040 b2        db      0B2h		;F104h => k�d 0E - p�epnut� z grafiky
					;                  do norm�ln�ho re�imu
f041 b2        db      0B2h		;F104h => k�d 0F - p�epnut� do grafick�ho re�imu
f042 19        db      019h		;F06Bh => k�d 10 - pouze RET
f043 19        db      019h		;F06Bh => k�d 11 - pouze RET
f044 b1        db      0B1h		;F103h => k�d 12 - p�epnut� z inverze
					;                  do norm�ln�ho re�imu
f045 b1        db      0B1h		;F103h => k�d 13 - p�epnut� do inverzn�ho re�imu
f046 19        db      019h		;F06Bh => k�d 14 - pouze RET
f047 19        db      019h		;F06Bh => k�d 15 - pouze RET 
f048 19        db      019h		;F06Bh => k�d 16 - pouze RET
f049 19        db      019h		;F06Bh => k�d 17 - pouze RET 
f04a 4c        db      04Ch		;F09Eh => k�d 18 - kurzor vpravo
f04b 53        db      053h		;F0A5h => k�d 19 - kurzor nahoru
f04c 59        db      059h		;F0ABh => k�d 1A - kurzor dol�
f04d 19        db      019h		;F06Bh => k�d 1B - pouze RET 
f04e 6e        db      06Eh		;F0C0h => k�d 1C - IC (Insert Column) - vytvo�en� mezery
					;mezi znaky na m�st� kurzoru a posun N znak�,
					;N je na adrese 0012
					;posun se zastavuje tak� na m�st� kde je znak 0D
f04f b7        db      0B7h		;F109h => k�d 1D - DC (Delete Column) - smaz�n� znaku
                                        ;na m�st� kurzoru s posuvem
f050 19        db      019h		;F06Bh => k�d 1E - pouze RET
f051 00        db      0		;F052h => k�d 1F - rutina maz�n� obrazovky (pomoc� " ")


;tisk ��d�c�ho k�du 1Fh - maz�n� obrazovky
;-----------------------------------------
f052 2a2000    ld      hl,(0020h)	;do HL adresa za��tku VIDEORAM
f055 3a1f00    ld      a,(001fh)	;do A po�et znak� na ��dek
f058 0620      ld      b,20h		;do B 32 - obrazovka m� 32 ��dk�
f05a 3620      ld      (hl),20h		;vlo� znak " " (mezera) do VIDEORAM (HL)
f05c 23        inc     hl		;dv� smy�ky pro AxB znak�
f05d 05        dec     b		
f05e c25af0    jp      nz,0f05ah
f061 3d        dec     a
f062 c258f0    jp      nz,0f058h	;opakuj do konce obrazovky a
					;pokra�uj nastaven�m pozice kurzoru

;tisk ��d�c�ho k�du 0Ch - kurzor na pozici 0,0 (HOME)
;----------------------------------------------------
f065 210000    ld      hl,0000h		;do HL 0
f068 220e00    ld      (000eh),hl	;vlo� na 000E a 000F - ��dek a sloupec kurzoru
f06b c9        ret     			;n�vrat

;tisk ��d�c�ho k�du 07 - p�pnut�
;-------------------------------
f06c c373f9    jp      0f973h		;sko� na vlastn� rutinu na F973

;tisk ��d�c�ho k�du 08 - kurzor vlevo
;------------------------------------
f06f 2a0c00    ld      hl,(000ch)	;do HL adresa m�sta na obrazovce
f072 2b        dec     hl		;sni� ji (doleva)
f073 3a2100    ld      a,(0021h)	;vy��� bajt adrey za��tku Videoram
f076 3d        dec     a		;sni�
f077 bc        cp      h		;porovnej s H
f078 c8        ret     z		;ned�lej nic a vra� se pokud si mimo Videoram
f079 3a1f00    ld      a,(001fh)	;do A po�et znak� na ��dek
f07c 4f        ld      c,a		;dej do C
f07d 3d        dec     a		;sni� o 1
f07e a5        and     l		;vymaskuj adresou
f07f 320e00    ld      (000eh),a	;nastav aktu�ln� sloupec
f082 79        ld      a,c		;po�et znak� na ��dce zp�t do A
f083 fe40      cp      40h		;je to 64?
f085 ca89f0    jp      z,0f089h		;pokud ano poposko� kousek
f088 29        add     hl,hl		;HLx2
f089 29        add     hl,hl		;HLx4 
f08a 29        add     hl,hl		;HLx8 (x4 pro V64)
f08b 7c        ld      a,h		;vy��� bajt do A
f08c e61f      and     1fh		;nech pouze 5 bit� kter� tvo�� aktu�ln� ��dek
f08e 320f00    ld      (000fh),a	;a ulo� na pot�i�n� m�sto (aktu�ln� ��dek) 
f091 67        ld      h,a		;ulo� si ��dek do H
f092 3a1300    ld      a,(0013h)	;d�lka str�nky do A
f095 3c        inc     a		;zvy� o 1
f096 bc        cp      h		;porovnej s aktu�ln�m ��dkem
f097 c0        ret     nz		;hotovo pokud se nerovnaj�
f098 2a0c00    ld      hl,(000ch)	;vyzvedni znovu adresu m�sta v obrazovce do HL
f09b c379f0    jp      0f079h		;jsme v prav�m doln�m rohu tak op�t nastav prom�nn�
					;(��dn� posun)

;tisk ��d�c�ho k�du 18h - kurzor vpravo
;--------------------------------------
f09e 2a0c00    ld      hl,(000ch)	;do HL adresa m�sta na obrazovce
f0a1 23        inc     hl		;zvy� ji (doprava)
f0a2 c379f0    jp      0f079h		;pokra�uj v rutin� Doleva

;tisk ��d�c�ho k�du 19h - kurzor nahoru
;--------------------------------------
f0a5 23        inc     hl		;zvy� HL na 0Fh (��dek kde je kurzor)
f0a6 7e        ld      a,(hl)		;aktu�ln� ��dek do A
f0a7 3d        dec     a		;sni� hodnotu o 1 (nahoru)
f0a8 f8        ret     m		;ned�l�j nic pokud si p�ijel na 255 (byla 0)
f0a9 77        ld      (hl),a		;ulo� sn�enou hodnotu ��sla akt. ��dku
f0aa c9        ret     			;n�vrat

;tisk ��d�c�ho k�du 1Ah - kurzor dol�
;------------------------------------
f0ab 23        inc     hl		;zvy� HL na 0Fh (��dek kde je kurzor)
f0ac 3a1300    ld      a,(0013h)	;d�lka str�nky do A
f0af be        cp      (hl)		;porovnej s aktu�ln�m ��dkem
f0b0 c8        ret     z		;pokud si na posledn�m tak n�vrat
f0b1 34        inc     (hl)		;jinak zvy� ��slo aktu�ln�ho ��dku
f0b2 c9        ret     			;a n�vrat

;tisk ��d�c�ho k�du 09 - tabul�tor 8 znak�
;-----------------------------------------
f0b3 7e        ld      a,(hl)		;vyzvedni hodnotu aktu�ln�ho sloupce
f0b4 e6f8      and     0f8h		;vynuluj spodn� 3 bity (jedeme po 8)
f0b6 c608      add     a,08h		;p�i�ti 8
f0b8 4f        ld      c,a		;ulo� si novou hodnotu do C
f0b9 3a1f00    ld      a,(001fh)	;do A po�et znak� na ��dek
f0bc 3d        dec     a		;sni� hodnotu (maska)
f0bd a1        and     c		;nech pouze dan� po�et bit� sloupce (nep�ekro�� ���ku)
f0be 77        ld      (hl),a		;ulo� novou hodnotu sloupce
f0bf c9        ret     			;n�vrat

;tisk ��d�c�ho k�du 1Ch - IC
;(Insert Column) - vytvo�en� mezery mezi znaky na m�st� kurzoru
;a posun N znak�, N je na adrese 0012
;posun se zastavuje tak� na m�st� kde je znak 0D
;--------------------------------------------------------------
f0c0 0620      ld      b,20h		;" " do B
f0c2 3a1200    ld      a,(0012h)	;po�et posunovan�ch znak� do A
f0c5 4f        ld      c,a		;a hned do C
f0c6 7e        ld      a,(hl)		;vyzvedni hodnotu aktu�ln�ho sloupce
f0c7 23        inc     hl		;posu� se na adresu prom�nn� ��dek kde je kurzor
f0c8 56        ld      d,(hl)		;aktu�ln� ��dek do D
f0c9 2a0c00    ld      hl,(000ch)	;do HL aktu�ln� adresa na obrazovce
;smy�ka podle C (po�et posunovan�ch znak�)
;-----------------------------------------
f0cc 0d        dec     c		;pokud nen� co posunovat (po�et je nula)
f0cd f8        ret     m		;n�vrat
f0ce e5        push    hl		;uschovej si adresu na z�sobn�k
f0cf 3c        inc     a		;zvy� sloupec o jedna
f0d0 211f00    ld      hl,001fh		;do HL adresa po�tu znak� na ��dek
f0d3 be        cp      (hl)		;porovnej se sloupcem
f0d4 c2f3f0    jp      nz,0f0f3h	;pokud se nerovnaj� posko� dop�edu
					;jinak posun na nov� ��dek
f0d7 3a1300    ld      a,(0013h)	;do A d�lka str�nky
f0da ba        cp      d		;jsi na posledn� ��dku?
f0db c2f2f0    jp      nz,0f0f2h	;sko� dop�edu pokud ne
f0de cd95f9    call    0f995h		;roluj obrazovku
f0e1 210f00    ld      hl,000fh		;sni� hodnutu
f0e4 35        dec     (hl)		;aktu�ln�ho ��dku (po odrolov�n�)
f0e5 00        nop     
f0e6 e1        pop     hl		;vyzvedni adresu ve videoram ze z�sobn�ku
f0e7 c5        push    bc		;uschovej po��tadlo
f0e8 3a1f00    ld      a,(001fh)	;v�po�et adresy ve videoram
f0eb 2f        cpl     			;kde budem pokra�ovat
f0ec 4f        ld      c,a		;v posunov�n� po odrolov�n�
f0ed 06ff      ld      b,0ffh		;(o ��dek v��)
f0ef 09        add     hl,bc		;ode�ti od HL d�lku ��dku
f0f0 c1        pop     bc		;obnov po��tadlo
f0f1 e5        push    hl		;ulo� adresu na z�sobn�k
f0f2 af        xor     a		;do A 0 (jsi na nov�m ��dku, sloupec nula)
f0f3 e1        pop     hl		;obnov adresu ve videoram
f0f4 5e        ld      e,(hl)		;vezmi sou�asnou hodnotu
f0f5 70        ld      (hl),b		;vlo� na jej� m�sto mezeru
f0f6 c5        push    bc		;uschovej po��tadlo
f0f7 4f        ld      c,a		;ulo� si do C aktu�ln� sloupec
f0f8 78        ld      a,b		;znak kter� jsme vkl�dali do A
f0f9 fe0d      cp      0dh		;byl to CR?
f0fb 79        ld      a,c		;do A aktu�ln� sloupec
f0fc c1        pop     bc		;vyzvedni po��tadlo
f0fd c8        ret     z		;n�vrat byl-li tam znak CR
f0fe 43        ld      b,e		;do B dej znak co byl na posunovan� pozici
					;pro p��t� vkl�d�n�
f0ff 23        inc     hl		;zvy� adresu ve videoram a 
f100 c3ccf0    jp      0f0cch		;opakuj dokud nejsou posunut� v�echny znaky

;tisk ��d�c�ho k�du 12/13 - p�epnut� z/do inverzn�ho re�imu
;-----------------------------------------------------
f103 13        inc     de		;zvy� de na 011h
f104 79        ld      a,c		;k�d do A
f105 e601      and     01h		;nech pouze 0-t� bit (0=vypnuto/1=zapnuto)
f107 12        ld      (de),a		;ulo� na pat�i�nou prom�nnou (011h)
f108 c9        ret     			;n�vrat

;tisk ��d�c�ho k�du 1Dh - DC
;(Delete Column) - smaz�n� znaku na m�st� kurzoru s posuvem
;----------------------------------------------------------
f109 3a1200    ld      a,(0012h)	;po�et posunovan�ch znak� do A
f10c 4f        ld      c,a		;a jako po��tadlo do C
f10d 0d        dec     c		;sni� po��tadlo o 1
f10e 46        ld      b,(hl)		;do B ��slo sloupce kde je kurzor (z 0Eh)
f10f 23        inc     hl		;posu� HL na prom�nnou ��slo ��dku
f110 56        ld      d,(hl)		;vyzvedni hodnotu akt. ��dku do D
f111 2a0c00    ld      hl,(000ch)	;do HL akt. adresa ve Videoram
f114 7e        ld      a,(hl)		;vyzvedni znak z dan�ho m�sta
f115 fe0d      cp      0dh		;je to CR?
f117 c8        ret     z		;pokud ano nen� co mazat a n�vrat
f118 0d        dec     c		;sni� po��tadlo
f119 fa34f1    jp      m,0f134h		;pokud bylo 0 odsko� (hotovo)
f11c 23        inc     hl		;zvy� adresu
f11d 04        inc     b		;zvy� ��slo sloupce
f11e 3a1f00    ld      a,(001fh)	;po�et znak� v ��dce do A
f121 b8        cp      b		;porovnej s akt. sloupcem
f122 c22df1    jp      nz,0f12dh	;pokud nejsi na konci ��dku posko�
f125 3a1300    ld      a,(0013h)	;d�lka str�nky do A
f128 ba        cp      d		;porovnej s akt. ��dkem
f129 c8        ret     z		;pokud jsi na posledn�m ��dku okam�it� n�vrat
f12a 0600      ld      b,00h		;jinak nastav sloupec na 0
f12c 14        inc     d		;posu� se na nov� ��dek
f12d 7e        ld      a,(hl)		;vyzvedni znak z Videoram
f12e 2b        dec     hl		;sni� adresu (posun doleva)
f12f 77        ld      (hl),a		;vlo� tam dan� znak
f130 23        inc     hl		;zvy� adresu
f131 c315f1    jp      0f115h		;opakuj dokud nejsou smaz�ny v�echyn pot�ebn� znaky
;po��tadlo bylo 0, hotovo
;------------------------
f134 360d      ld      (hl),0dh		;vlo� do Videoram na akt. pozici CR
f136 c9        ret     			;n�vrat

;tisk ��d�c�ho k�du 0Dh - CR
;od��dkov�n� a zru�en� grafick�ho i inverzn�ho re�imu
;----------------------------------------------------
f137 3600      ld      (hl),00h		;nastav aktu�ln� sloupec na 0
f139 210000    ld      hl,0000h		;nastav re�im na norm�ln�
f13c 221000    ld      (0010h),hl	;(nen� inverze ani grafika)
f13f 2a0c00    ld      hl,(000ch)	;aktu�ln� adresa ve videoram do HL
f142 71        ld      (hl),c		;vlo� tam k�d CR
f143 3a1400    ld      a,(0014h)	;do A hodnota ��dkov�n�
f146 4f        ld      c,a		;schovej si ji do C jako po��tadlo
f147 210f00    ld      hl,000fh		;adresa prom�nn� ��dek kde je kurzor do HL
f14a 3a1300    ld      a,(0013h)	;d�lka str�nky do A
f14d be        cp      (hl)		;porovnej aktu�ln� ��dku s d�lkou str�nky
f14e c255f1    jp      nz,0f155h	;pokud nejsi na posledn� ��dce posko�
f151 cd95f9    call    0f995h		;odroluj obrazovku
f154 fe        db      0FEh		;f�gl k p�esko�en� n�sleduj�c� instrukce (cp N)
					;tzn. neprovede se inkrementace pokud bylo Call F995
f155 34        inc     (hl)		;zvy� ��slo akt. ��dku je-li to pot�eba (ne pokud
					;se rolovalo)
f156 0d        dec     c		;sni� po��tadlo ��dkov�n�
f157 c247f1    jp      nz,0f147h	;opakuj dokud nen� od��dkov�n dan� po�et ��dk�
f15a c9        ret     			;n�vrat

;pokra�ov�n� F007 - tisk znaku s k�dem 32 a vy���m
;-------------------------------------------------
f15b eb        ex      de,hl		;HL=010h, DE=0Eh
f15c 7e        ld      a,(hl)		;vyzvedni prom�nnou graf./norm. re�im
f15d e601      and     01h		;otestuj jak� je re�im
f15f ca6af1    jp      z,0f16ah		;odsko� pro norm�ln� re�im
					;jinak grafick� re�im
f162 79        ld      a,c		;do A znak co se bude tiskout
f163 d640      sub     40h		;ode�ti 64
f165 f8        ret     m		;pokud to bylo 32-63 n�vrat 
f166 fe20      cp      20h		;m�me jen 31 gr. znak�
f168 f0        ret     p		;pokud je to 32 a v�c op�t n�vrat bez tisku
f169 4f        ld      c,a		;dej znak do C
;norm�ln� re�im (grafick� znaky 64-95 p�ek�dov�ny na 0-31)
;---------------------------------------------------------
f16a 23        inc     hl		;posu� se na prom�nnou inverz./norm. re�im
f16b 7e        ld      a,(hl)		;otestuj
f16c e601      and     01h		;re�im
f16e 79        ld      a,c		;do A znak co se bude tisknout
f16f ca74f1    jp      z,0f174h		;pro norm�ln� re�im odsko� d�l
f172 f680      or      80h		;nastav bit 7 pro inv. re�im
f174 2a0c00    ld      hl,(000ch)	;vyzvedni aktu�ln� adresu ve videoram
f177 77        ld      (hl),a		;vlo� na jej� m�sto dan� znak
f178 210e00    ld      hl,000eh		;do HL adresa prom�nn� akt. sloupec
f17b 34        inc     (hl)		;zvy� hodnotu akt. sloupce
f17c 3a1f00    ld      a,(001fh)	;vyzvedni po�et znak� na ��dek
f17f 3d        dec     a		;sni� o jedna (0-x)
f180 be        cp      (hl)		;jsi za posledn�m znaku ��dku?
f181 f0        ret     p		;n�vrat pokud ne
f182 3600      ld      (hl),00h		;nov� ��dek tedy sloupec nastav 0
f184 23        inc     hl		;posu� se na aktu�ln� ��dek
f185 34        inc     (hl)		;zvy� hodnotu ��dku
f186 3a1300    ld      a,(0013h)	;vyzvedni d�lku str�nky
f189 be        cp      (hl)		;porovnej s akt. ��dkem
f18a f0        ret     p		;a pokud net�eba rolovat n�vrat
f18b 35        dec     (hl)		;jinak sni� akt. ��dek
f18c cd95f9    call    0f995h		;roluj obrazovku nahoru 
f18f c9        ret     			;n�vrat

;Sem sk��e rutina F007 po vyti�t�n� znaku
;----------------------------------------
f190 0600      ld      b,00h		;vynuluj B
f192 3a1f00    ld      a,(001fh)	;po�et znak� na ��dku do A
f195 4f        ld      c,a		;a do C
f196 3a0f00    ld      a,(000fh)	;do A akt. ��dek
f199 2a2000    ld      hl,(0020h)	;do HL adresa Videoram
f19c fe        db      0FEh		;f�gl: neprov�d�j "add" p�ed prvn�m "dec A" (cp N)
f19d 09        add     hl,bc		;vypo��tej adresu ��dku ve Videoram
f19e 3d        dec     a		;sou�tem aktu�ln�-��dek x po�et znak� na ��dku
f19f f29df1    jp      p,0f19dh		;opakuj pro v�ecky ��dky
f1a2 3a0e00    ld      a,(000eh)	;vyzvedni akt. sloupec
f1a5 4f        ld      c,a		;dej do C (B=0)
f1a6 09        add     hl,bc		;a dopo��tej v�slednou adresu akt. znaku do HL
f1a7 220c00    ld      (000ch),hl	;ulo� na pat�i�nou prom�nnou
f1aa 7e        ld      a,(hl)		;vyzvedni aktu�ln� znak na pozici kurzoru
f1ab 320b00    ld      (000bh),a	;a dej ho na pat�i�nou prom�nnou
f1ae e1        pop     hl		;obnov ulo�en� registry p�ed tiskem a
f1af d1        pop     de
f1b0 c1        pop     bc
f1b1 f1        pop     af
f1b2 c9        ret     			;konec F007 (tisk znaku z C v�etn� ��d�c�ch k�d�)

;pokra�ov�n� startu - inicializace 8259
;--------------------------------------
f1b3 e1        pop     hl		;vyzvedni adresu z�sobn�ku do HL 
f1b4 3ef7      ld      a,0f7h		;inicializace 8259, re�im s n�stupnou hranou
					;a potla�en�m p�eru�en� stejn� a ni��� �rovn�
					;1111 0111
					;111-adresa a7-a5  1-ICW1 0-hrana 1-po 4 bajtech
					;1-single  1-ICW4 ano
f1b6 d388      out     (88h),a		;po�li ICW1
f1b8 7c        ld      a,h		;nastav adresu tabulky rutin p�eru�en� (a8-a15)
f1b9 d389      out     (89h),a		;ICW2 - podle konce RAM - registr H (7F pro 32kb)
f1bb af        xor     a		;re�im s manu�ln�m ukon�ov�n�m p�eru�en�
					;0000 0000
					;000 0-ne specialn� re�im 00 - ne bufferovan� re�im
					;0-manu�ln� konec 0-8080(5) re�im
f1bc d389      out     (89h),a		;po�li ICW4
f1be 3e9f      ld      a,9fh		;maska p�eru�en� 10011111 - povoluje p�eru�en� 5 a 6
d1c0 d389      out     (89h),a		;OCW1 - co� jsou 50Hz a tla��tko BR
f1c2 3e20      ld      a,20h		;povol p�eru�en� 8259 (p��kaz konec p�eru�en�)
f1c4 d388      out     (88h),a		;OCW2 0010 0000
					;001 - nespecifikovan� konec p�eru�en�
f1c6 fb        ei      			;povol p�eru�en� procesoru
f1c7 3a00c8    ld      a,(0c800h)	;test jestli je p��tomen
f1ca 3c        inc     a		;modul Basic6, Basic G nebo jin�
f1cb c200c8    jp      nz,0c800h	;pokud ano pokra�uj t�mto modulem na C800h
f1ce 2136f2    ld      hl,0f236h	;jinak je monitor a vypi�
f1d1 cd88f4    call    0f488h		;hl�en� "Monitor >"

;hlavn� smy�ka monitoru
;----------------------
f1d4 2a0400    ld      hl,(0004h)	;obnov vrchol z�sobn�ku podle konce RAM
f1d7 2ec2      ld      l,0c2h
f1d9 f9        ld      sp,hl		;nastav SP
f1da 3e9f      ld      a,9fh		;odmaskuj p�eru�en� 5 a 6
f1dc d389      out     (89h),a		;na 8259
f1de fb        ei      			;povol p�eru�en�
f1df cd47f6    call    0f647h		;tisk n�sleduj�c�ho znaku
f1e2 3e        db      ">"
f1e3 cd00f0    call    0f000h  		;znak z kl�vesnice na obrazovku a do C
f1e6 79        ld      a,c		;k�d kl�vesy do A pro porovn�n�
f1e7 2109f2    ld      hl,0f209h	;tabulka p�smenek p��kaz� monitoru do HL
f1ea 010a00    ld      bc,000ah		;je 10 p��kaz�
f1ed be        cp      (hl)		;porovnat k�d kl�vesy s tabulkou
f1ee caf9f1    jp      z,0f1f9h		;shoda, pokra�uj v�po�tem adresy p��kazu
f1f1 23        inc     hl		;posun na dal�� znak v tabulce
f1f2 0d        dec     c		;opakujeme 10x
f1f3 c2edf1    jp      nz,0f1edh	;do konce tabulky
f1f6 c327f2    jp      0f227h		;nenalezeno, vypi� ? a jdi zp�t na hlavn� smy�ku


;pokra�ov�n� hlavn� smy�ky p�i nalezen� p��kazu monitoru
;-------------------------------------------------------
f1f9 2111f2    ld      hl,0f211h	;adresa tabulky p��kaz� monitoru -2
f1fc 09        add     hl,bc		;po��t�me adresu podle po�ad� p�smenka v tabulce
f1fd 09        add     hl,bc
f1fe 01d4f1    ld      bc,0f1d4h	;n�vratov� adresa (smy�ka monitoru)
f201 c5        push    bc		;na z�sobn�k
f202 0602      ld      b,02h		;v�t�ina p��kaz� m� 2 parametry (dle pot�eby se m�n�
					;v jednotliv�ch rutin�ch)
f204 7e        ld      a,(hl)		;vyzvedni adresu
f205 23        inc     hl		;p��kazu monitoru z tabulky
f206 66        ld      h,(hl)		;do registru HL
f207 6f        ld      l,a
f208 e9        jp      (hl)		;a sko� na n�

;tabulka p�smenek p��kaz� monitoru
;---------------------------------
f209 52534347  db      "RSCG"		;jednop�smenov� p��kazy
f20d 4c445857  db      "LDXW" 
f212 4d46      db      "MF"

;tabulka odpov�daj�c�ch adres p��kaz� monitoru
;---------------------------------------------
f213 51f2      dw      0f251h		;F - Fill - napln�n� ��sti pam�ti hodnotou
f215 40f2      dw      0f240h		;M - Move - p�esun ��sti pam�ti
f217 60f2      dw      0f260h		;W - Write - ulo�en� na magnetofon
f219 c4f2      dw      0f2c4h		;X - Change - zm�na obsahu registr�/jejich v�pis
f21b 85f3      dw      0f385h		;D - Display - vyp�e obsah ��sti pam�ti
f21d b6f3      dw      0f3b6h		;L - Load - z magnetofonu
f21f 05f4      dw      0f405h		;G - Goto - skok na adresu
f221 fcf3      dw      0f3fch		;C - Call - zavol�n� podprogramu na adrese
f223 38f4      dw      0f438h		;S - Subst - m�n� obsah m�sta v pam�ti
f225 84f4      dw      0f484h		;R - Return - n�vrat do Basicu (tepl� start)


;nezn�m� p��kaz monitoru
;-----------------------
f227 cd47f6    call    0f647h		;po�li na v�stupn� za��zen� znak ?
f22a 3f        db      "?"		;proto�e takov� p��kaz monitor nezn�
f22b cd47f6    call    0f647h		;po�li na v�stupn� za��zen� znak od��dkuj
f22e 0a        db      0dh		;
f22f cd47f6    call    0f647h		;po�li na v�stupn� za��zen� znak n�vrat voz�ku
f232 0a        db      0ah	
f233 c3d4f1    jp      0f1d4h		;zp�t na smy�ku monitoru (obnovit z�sobn�k a �ekat na
					;kl�vesu s p��kazem monitoru)

;text hl�en� monitoru
----------------------
f236 0d        db      0dh		;od��dkuj a nastav z�kladn� re�im
f237 1f        db      01f		;sma� obrazovku a kurzor na 0,0     
f238 4d4f4e    db      "MONITOR"
f23b 49544f52
f23f 8d        db      0dh+80h		;od��dkuj s nastaven�m nejvy���m bitem jako konec textu

;p��kaz M monitoru - P�esun bloku pam�ti
;m� 3 parametry - od do kam
;---------------------------------------
f240 04        inc     b		;zvy� po�et parametr�
f241 cde5f4    call    0f4e5h		;vyzvedni parametry
f244 c1        pop     bc		;a dej je do odpov�daj�c�ch registr�
f245 d1        pop     de
f246 e1        pop     hl

;rutina na p�enos bajt� z adresy od HL do DE na adresu danou BC
;--------------------------------------------------------------
f247 7e        ld      a,(hl)		;kopie bajtu z (HL)
f248 02        ld      (bc),a		;na (BC)
f249 03        inc     bc		;posun na dal�� c�lovou adresu
f24a cd9bf4    call    0f49bh		;zvy� HL a otestuj HL=0 nebo HL=DE
f24d d247f2    jp      nc,0f247h	;pokud ne kop�ruj dal�� bajt 
f250 c9        ret     			;hotovo

;p��kaz F monitoru - Napln�n� ��sti pam�ti hodnotou
;m� 3 parametry - od do ��m
;--------------------------------------------------
f251 04        inc     b		;zvy� po�et parametr�
f252 cde5f4    call    0f4e5h		;vyzvedni parametry
f255 c1        pop     bc		;a dej je do odpov�daj�c�ch registr�
f256 d1        pop     de
f257 e1        pop     hl
f258 71        ld      (hl),c		;dej na danou adersu po�adovan� znak
f259 cd9bf4    call    0f49bh		;zvy� HL a otestuj HL=0 nebo HL=DE
f25c d258f2    jp      nc,0f258h	;opakuj dokud nen� v�e vypln�no
f25f c9        ret     			;n�vrat

;P��kaz W monitoru
;nahr�n� dat na magnetofon - parametry: od, do, start (0 pro nestart)
;pou��v� F66B - 4.5. bit IO bajtu na rozli�en� periferie (st. magnetofon)
;kontroln� sou�et je sou�et v�ech bajt� bloku se zanedb�n�m p�enosu p�es 256
;na konci invertovan� a p�i�tena 1�ka
;jeden blok vypad� takto:
;1. bajt ":" (1 bajt)
;2. po�et bajt� X v bloku (1 bajt)
;3. adresa kam se nahr�v� (2 bajty) (pro z�v�re�n� blok je tady startovac� adresa)
;4. odd�lova� - 0 pro norm�ln� blok- 80 bajt� nebo posledn� do konce nahr�van� oblasti
;               1 pro z�v�re�n� blok (1 bajt) (z�v�re�n� blok nem� ��dn� data)
;5. jednotliv� datov� bajty bloku (X kr�t 1 bajt)
;6. kontroln� sou�et (1 bajt)
;7. 2 bajty 0Dh a 0Ah
;-------------------------------------------------------------------------------------
f260 04        inc     b		;3 parametry
f261 cde5f4    call    0f4e5h		;vyzvedni je
f264 c1        pop     bc		;a dej do odpov�daj�c�ch registr�
f265 d1        pop     de
f266 e1        pop     hl
;nahr�n� dat na magnetofon od HL do DE, startovac� adresa v BC
--------------------------------------------------------------
f267 c5        push    bc		;uschovej startovac� adresu (0 pokud se nestartuje)
f268 cdfcf6    call    0f6fch		;start magnetofonu nebo d�rova�e p�i nahr�v�n�
f26b e5        push    hl		;uschovej adresu kam budem nahr�vat blok
f26c 015000    ld      bc,0050h		;nahr�v�me bloky o 50h bajtech
f26f 09        add     hl,bc		;p�i�ti k adrese kam se bude nahr�vat
f270 cda0f4    call    0f4a0h		;porovnej zda je v�sledek v�t�� ne� koncov� adresa
					;pokud ano je CY=1
f273 e1        pop     hl		;obnov adresu kam se bude nahr�vat
f274 79        ld      a,c		;do A d�me 80 (50h) - po�et bajt� v bloku
f275 d27bf2    jp      nc,0f27bh	;pokud je�t� nenahr�v�me posledn� blok odsko� d�l
f278 7b        ld      a,e		;jinak uprav po�et znak� v bloku tak 
f279 95        sub     l		;aby odpov�dal koncov� adrese
f27a 3c        inc     a
f27b d5        push    de		;uschovej koncovou adresu
f27c 47        ld      b,a		;po��tadlo bajt� do B
f27d 1600      ld      d,00h		;D se pou��v� jako pomocn� pro kontroln� sou�et
					;tak�e si ho tady p�ed ka�d�m blokem vynulujeme
f27f cd67f6    call    0f667h		;po�li na periferii n�sleduj�c� znak (podle F66B)
f282 3a	       db      03ah		;bajt ":"
f283 78        ld      a,b		;po�et bajt� v bloku do A
f284 cde8f5    call    0f5e8		;po�li na periferii bajt z A (podle F66B)
f287 b3        or      e		;pokud je v A i E nula je konec
f288 caacf2    jp      z,0f2ach		;a zapi� z�v�re�n� ukon�ovac� blok 
f28b cde3f5    call    0f5e3h		;adresa v HL na periferii
f28e af        xor     a		;od�lova� 0 do A - nen� posledn� blok (ten m� 1-�ku)
f28f cde8f5    call    0f5e8h		;a na periferii
;smy�ka pro blok
f292 7e        ld      a,(hl)		;vyzvedni z adresy bajt;
f293 23        inc     hl		;zvy� adresu
f294 cde8f5    call    0f5e8h		;po�li ho na periferii
f297 05        dec     b		;sni� po��tadlo
f298 c292f2    jp      nz,0f292h	;opakuj do konce bloku
f29b 2f        cpl     			;vypo��t�me hodnotu
f29c 3c        inc     a		;kontroln�ho sou�tu
f29d cde8f5    call    0f5e8h		;a vypi� na periferii
f2a0 cd67f6    call    0f667h		;n�sleduj�c� bajt - 0Dh - na periferii
f2a3 0d        db      0dh
f2a4 cd67f6    call    0f667h		;n�sleduj�c� bajt - 0Ah - na periferii
f2a7 0a        db      0ah
f2a8 d1        pop     de		;obnov uschovanou koncovou adresu
f2a9 c36bf2    jp      0f26bh		;zp�tky na p��pravu dal��ho bloku (bu� cel� nebo 
					;��st do konce posledn�)

f2ac e1        pop     hl		;koncovou adresu u� nepot�ebujeme /pry� ze z�sobn�ku
f2ad e1        pop     hl		;vyzvedneme ulo�enou startovac� adresu (0 nestart)
f2ae cde3f5    call    0f5e3h		;a po�leme ji na periferii
f2b1 3e01      ld      a,01h		;z�v�re�n� blok obsahuje pouze bajt s hodnotou 1
f2b3 cde8f5    call    0f5e8h		;a dej ho na periferii
f2b6 2f        cpl     			;vypo�ti kotroln�
f2b7 3c        inc     a		;sou�et
f2b8 cde8f5    call    0f5e8h		;a dej ho na periferii
f2bb cd67f6    call    0f667h		;n�sleduj�c� bajt - 0Dh - na periferii
f2be 0d        db      0dh
f2bf cd67f6    call    0f667h		;n�sleduj�c� bajt - 0Ah - na periferii
f2c2 0a        db      0ah		;(od��dkov�n� a p�esun na za��tek dal��ho ��dku)
f2c3 c9        ret     			;n�vrat - hotovo ulo�eno

;p��kaz X monitoru (jak bez tak s parametrem)
;bez parametru pouze vyp�e obsah registr� ulo�en�ch na 0022-002D,
;jinak zm�na obsahu registru
;-----------------------------------------------------------------
f2c4 cd00f0    call    0f000h		;na�ti a vytkni dal�� znak z kl�vesnice
f2c7 79        ld      a,c		;dej ho do A pro test jestli
f2c8 fe0d      cp      0dh		;je to CR? (konec ��dku)
f2ca c2edf2    jp      nz,0f2edh	;pokud ne odsko� d�l na X s parametrem
					;jinak je pouze
;v�pis obsahu registr� z adres 0022-002D 
----------------------------------------
f2cd 2135f3    ld      hl,0f335h	;do HL tabulka s n�zvy registr� a od��dkov�n�
f2d0 cd88f4    call    0f488h		;vytiskni tabulku
f2d3 f3        di			;zaka� p�eru�en�     
f2d4 112200    ld      de,0022h		;0022 - adresa bufferu registr�
f2d7 0606      ld      b,06h		;tiskneme 6x postupn� obsah hodnot v�ech registr�
f2d9 eb        ex      de,hl		;kter� jsou na adres�ch 0022-002D
f2da 5e        ld      e,(hl)		;AF, BC, DE, HL, SP, PC 
f2db 23        inc     hl
f2dc 56        ld      d,(hl)
f2dd 23        inc     hl
f2de eb        ex      de,hl
f2df cdd0f5    call    0f5d0h		;vypi� HL hexadecim�ln� (4 znaky)
f2e2 cd47f6    call    0f647h		;vytiskni mezeru
f2e5 20        db      " "
f2e6 05        dec     b		;u� je v�ech 6?
f2e7 c2d9f2    jp      nz,0f2d9h	;pokud ne tiskni dal��
f2ea c32bf2    jp      0f22bh		;od��dkuj a sko� do hlavn� smy�ky monitoru

;X - modifikace obsahu registru
;p��kazy XA, XF, XB, XC, XD, XE, XH, XL, XS (pro SP) a XP (pro PC) 
;jm�no registru jako znak v A 
;----------------------------------------------------------------
f2ed 2166f3    ld      hl,0f366h	;tabulka registr� a jejich adres
f2f0 0e0a      ld      c,0ah		;m�me 10 registr� kter� je mo�no m�nit
f2f2 be        cp      (hl)		;hled�me kter� se m�n�
f2f3 ca00f3    jp      z,0f300h		;pokud je to on odsko� d�l
f2f6 23        inc     hl		;posun v tabulce
f2f7 23        inc     hl
f2f8 23        inc     hl
f2f9 0d        dec     c		;projedem celou tabulku
f2fa c2f2f2    jp      nz,0f2f2h	;dokud nejsou vyzkou�eny v�echny mo�nosti
f2fd c327f2    jp      0f227h		;nezn�m� p��kaz (�patn� jm�no registru)
					;vyp�e otazn�k, od��dkuje a pak smy�ka monitoru
;pokra�ov�n� X p�i nalezen� kter� registr se bude m�nit
f300 cd47f6    call    0f647h		;vypi� " "
f303 20        db      " "
f304 cd54f3    call    0f354h		;vypi� obsah dan�ho registru (SP PC jako celek)
					;a dej jeho adresu do DE a do B FF/0 podle po�tu bajt�
f307 cd47f6    call    0f647h		;vypi� "-"
f30a 2d        db      "-"
f30b cd00f0    call    0f000h		;znak z kl�vesnice rovnou vytisknout (a do A)
f30e 79        ld      a,c
f30f cdb7f4    call    0f4b7h		;otestuj CR/SP
f312 d8        ret     c		;n�vrat pokud bylo CR
f313 ca29f3    jp      z,0f329h		;pokud je SP pokra�uj d�l (posu� se na dal�� registr)
f316 e5        push    hl		;uschovej HL
f317 c5        push    bc		;uschovej BC (v C znak)
f318 210000    ld      hl,0000h		;po��te�n� hodnota ��sla na 0
f31b cdc9f4    call    0f4c9h		;na�ti ��slo z C a dal�� ��slice z kl�vesnice do HL
f31e 7d        ld      a,l		;vezmi hodnotu registru (0-FF)
f31f 12        ld      (de),a		;a dej ji na prom�nnou dan�ho registru
f320 f1        pop     af		;v B a te� v A je po�et bajt� (1/2>255/0)
f321 b7        or      a		;test
f322 fa28f3    jp      m,0f328h		;�lo-li o jednobajtov� registr posko�
f325 13        inc     de		;jinak ulo�
f326 7c        ld      a,h		;vy��� bajt na
f327 12        ld      (de),a		;prom�nnou dan�ho registru
f328 e1        pop     hl		;obnov HL
f329 af        xor     a		;do A dej 0
f32a b6        or      (hl)		;vyzvedni dal�� hodnotu z tabulky registr�
f32b fab9f5    jp      m,0f5b9h		;pokud si na konci n�vrat p�es od��dkov�n�
f32e 79        ld      a,c		;dej do A posledn� znak z kl�vesnice
f32f fe0d      cp      0dh		;pokud to byl CR
f331 c8        ret     z		;tak n�vrat
f332 c304f3    jp      0f304h		;jinak opakuj pro dal�� registr

;tabulka s n�zvy registr� a od��dkov�n�
;--------------------------------------
f335 2041      db      " A"
f337 2046      db      " F"
f339 20        db      " "
f33a 2042      db      " B"
f33c 2043      db      " C"
f33e 20        db      " "
f33f 2044      db      " D"
f341 2045      db      " E"
f343 20        db      " "
f344 2048      db      " H"
f346 204c      db      " L"
f348 2020      db      "  "
f34a 20        db      " "
f34b 2053      db      " S"
f34d 2020      db      "  "
f34f 20        db      " "
f350 2050      db      " P"
f352 0d8a      db      0Dh,0Ah+80h

;X - vypi� obsah dan�ho registru (SP PC jako celek)
;v HL je adresa s pozic� znaku registru v tabulce F366
;nastav� DE na adresu registru v prom�nn�ch monitoru
;a B podle d�lky (FF/0 pro 1/2 bajty)
;-----------------------------------------------------
f354 23        inc     hl		;posu� se na doln� bajt adresy
f355 5e        ld      e,(hl)		;adresu dan�ho registru z bufferu registr�
f356 1600      ld      d,00h		;dej do DE
f358 23        inc     hl		;posu� se na dal�� bajt, co� je indik�tor po�tu bajt� 
f359 46        ld      b,(hl)		;kter� se budou tisknout (1 je pro SP, PC - 2 bajty)
f35a 23        inc     hl		;a posu� se na dal�� registr
f35b 1a        ld      a,(de)		;vyzvedni hodnotu dan�ho registru z bufferu
f35c cdd5f5    call    0f5d5h		;vytiskni ji (v HEX tvaru)
f35f 05        dec     b		;test jestli je�t� tisknout / SP nebo PC
					;do B dej FF pro 1 bajt a 0 pro 2
f360 f8        ret     m		;pokud ne vra� se
f361 1b        dec     de		;dal�� bajt na tisknut� je o 1 n� v bufferu	
f362 1a        ld      a,(de)		;vyzvedni jeho hodnotu
f363 c3d5f5    jp      0f5d5h		;a vytiskni ji s n�vratem zp�t (obvykle na F307)


;tabulka registr� a jejich adres a po�tu bajt� kter� tisknout
;0=1 bajt, 1=2 bajty (SP,PC)
;------------------------------------------------------------
f366 412300    db      "A",023h,0
f369 462200    db      "F",022h,0
f36c 422500    db      "B",025h,0
f36f 432400    db      "C",024h,0
f372 442700    db      "D",027h,0
f375 452600    db      "E",026h,0
f378 482900    db      "H",029h,0
f37b 4c2800    db      "L",028h,0
f37e 532b01    db      "S",02Bh,1
f381 502d01    db      "P",02Dh,1
f384 ff

;p��kaz monitoru D
;-----------------
f385 05        dec     b		;jen jeden parametr (adresa)
f386 cde5f4    call    0f4e5h		;na�ti ho
f389 e1        pop     hl		;a dej do HL
;D od adresy v HL
-----------------
f38a 3a1300    ld      a,(0013h)	;vyzvedni d�lku str�nky
f38d 0f        rrca    			;d�l�me 2
f38e 47        ld      b,a		;dej do B
f38f 05        dec     b		;a sni� B o jedna
f390 caabf3    jp      z,0f3abh		;odsko� pry� pokud je obrazovka u� zapln�n�
f393 cdb9f5    call    0f5b9h		;od��dkuj podle F6AB
f396 cdfaf5    call    0f5fah		;vypi� HL podle F6AB
f399 cda7f6    call    0f6a7h		;vytiskni n�sleduj�c� bajt podle F6AB
f39c 207e      db      " "		;mezera
f39c 7e        ld      a,(hl)		;vyzvedni obsah z HL (dan� parametr)
f39e cdfff5    call    0f5ffh		;vypi� A podle F6AB
f3a1 23        inc     hl		;zvy� adresu
f3a2 7d        ld      a,l		;ni��� bajt do a
f3a3 e607      and     07h		;nech pouze spodn� 3 bity (0-7)
f3a5 c299f3    jp      nz,0f399h	;opakuj dokud jich nen� 8 na ��dku
f3a8 c38ff3    jp      0f38fh		;opakuj do zapln�n� obrazovky na nov�m ��dku

;u� je zapln�n� cel� obrazovka
;-----------------------------
f3ab cd2bf6    call    0f62bh		;vstup z periferie podle 2 ni���ch bit� IO
f3ae fe0d      cp      0dh		;je to CR?
f3b0 cab9f5    jp      z,0f5b9h		;pokud ano n�vrat p�es od��dkov�n� podle F6AB
f3b3 c38af3    jp      0f38ah		;jinak opakuj v�pis

;p��kaz L monitoru- Load z magnetofonu/d�rn� p�sky
;=================================================
f3b6 05        dec     b		;pouze jeden parametr
f3b7 cde5f4    call    0f4e5h		;vyzvedni jej a dej na z�sobn�k
f3ba dbf8      in      a,(0f8h)		;�ti port A modulu Staper - �te�ka d�rn� p�sky
f3bc cd11f7    call    0f711h		;start magnetofonu a p��prava obrazovky na load
f3bf cd0bf5    call    0f50bh		;vstup bajtu do A z periferie podle F689
f3c2 d63a      sub     3ah		;ode�ti 58 (":")
f3c4 c2bff3    jp      nz,0f3bfh	;opakuj dokud nen� dvojte�ka
f3c7 57        ld      d,a		;kontroln� sou�et nastavit na 0
f3c8 cdf4f4    call    0f4f4h		;2 ��slice v Ascii jako bajt do A podle F689
f3cb 5f        ld      e,a		;dej do E (po�et bajt�)
f3cc cdf4f4    call    0f4f4h		;2 ��slice v Ascii jako bajt do A podle F689
f3cf 67        ld      h,a		;do H (vy��� bajt adresy)
f3d0 cdf4f4    call    0f4f4h		;2 ��slice v Ascii jako bajt do A podle F689
f3d3 6f        ld      l,a		;do L (ni��� bajt adresy)
f3d4 7b        ld      a,e		;test po�tu bajt� v bloku
f3d5 a7        and     a		;na nulu
f3d6 c2e4f3    jp      nz,0f3e4h	;pokud to nen� posledn� blok (nulov� d�lka) odsko�
f3d9 7d        ld      a,l		;v p��pad� posledn�hi bloku je v HL adresa autostartu
f3da b4        or      h		;ale nestartovat pokud je 0
f3db d1        pop     de		;do DE dej parametr p��kazu L
f3dc cad4f1    jp      z,0f1d4h		;pokud nen� autostart, sko� na hlavn� smy�ku monitoru
f3df 3e9f      ld      a,9fh		;jinak odmaskuj p�eru�en�
f3e1 d389      out     (89h),a
f3e3 e9        jp      (hl)		;a sko� na danou adresu

;�ten� bloku dat z periferie (mgf/d�rn� p�sky)
;---------------------------------------------
f3e4 c1        pop     bc		;do BC parametr p��kazu (nebo 0 pokud nen�)
f3e5 c5        push    bc		;a zp�tky na z�sobn�k pro p��t� pou�it�
f3e6 09        add     hl,bc		;p�i�ti k adrese dan�ho bloku
f3e7 cdf4f4    call    0f4f4h		;2 ��slice v Ascii jako bajt do A podle F689
					;(odd�lovac� bajt)
f3ea cdf4f4    call    0f4f4h		;2 ��slice v Ascii jako bajt do A podle F689
f3ed 77        ld      (hl),a		;ulo� dan� bajt na jeho m�sto
f3ee 23        inc     hl		;zvy� adresu
f3ef 1d        dec     e		;sni� po��tadlo bajt� v bloku
f3f0 c2eaf3    jp      nz,0f3eah	;opakuj pro v�echny bajty bloku
f3f3 cdf4f4    call    0f4f4h		;na�ti kontroln� sou�et (2 ��slice podle F689)
f3f6 cabff3    jp      z,0f3bfh		;a pokud je v�e OK sko� na �ten� dal��ho bloku
f3f9 c327f2    jp      0f227h		;vypi� ? p�i chyb� a zp�t do smy�ky monitoru

;p��kaz C monitoru
;=================
f3fc 2a2a00    ld      hl,(002ah)	;do HL obsah syst�mov� prom�nn� SP na 02Ah
f3ff 1132f4    ld      de,0f432h	;dej na vrchol z�sobn�ku
f402 73        ld      (hl),e		;n�vratovou adresu
f403 23        inc     hl		;0F432h
f404 72        ld      (hl),d
f405 cd00f0    call    0f000h		;znak z kl�vesnice na obrazovku (I/O=69)
f408 79        ld      a,c		;dej si znak do A
f409 cdb7f4    call    0f4b7h		;otestuj " ","," a CR
f40c da1ef4    jp      c,0f41eh		;pokud je pouze C odsko� d�le (C a CR)
f40f ca27f2    jp      z,0f227h		;nen� povolena ani mezera ani ��rka, odko� na chybu
f412 210000    ld      hl,0000h		;vynuluj HL
f415 cdc9f4    call    0f4c9h		;vyzvedni adresu podprogramu kter� zavolat
f418 d227f2    jp      nc,0f227h	;pokud je �patn� skok na chybu
f41b 222c00    ld      (002ch),hl	;ulo� do syst�mov� prom�nn� PC na 02Ch
f41e f3        di      			;zaka� p�eru�en�
f41f 312200    ld      sp,0022h		;vyzvedni registry podle syst�mov�ch prom�nn�ch
f422 f1        pop     af		;od adresy 022h
f423 c1        pop     bc
f424 d1        pop     de
f425 2a2a00    ld      hl,(002ah)	;vyzvedni SP
f428 f9        ld      sp,hl
f429 2a2c00    ld      hl,(002ch)	;vyzvedni adresu podprogramu kter� volat
f42c e5        push    hl		;dej ji na z�dobn�k
f42d 2a2800    ld      hl,(0028h)	;vyzvedni HL
f430 fb        ei      			;povol p�eru�en�
f431 c9        ret     			;skok na podprogram

;n�vrat po C sem
;---------------
f432 cd0df6    call    0f60dh		;ulo� registry do prom�nn�ch monitoru
f435 c3d4f1    jp      0f1d4h		;skok zp�t do hlavn� smy�ky monitoru

;p��kaz S monitoru - zm�na obsahu pam�ti
;pozor, neodes�l� se CR ale SP !!!!
;=======================================
f438 cdc3f4    call    0f4c3h		;vstup ��sla z kl�vesnice do HL
f43b d8        ret     c		;n�vrat pokud CR (S mus� b�t zakon�en� mezerou!)
f43c fe2c      cp      2ch		;je to ","?
f43e ca27f2    jp      z,0f227h		;vypi� ? pokud ano a zp�t do smy�ky monitoru
f441 cdb0f5    call    0f5b0h		;od��dkuj podle F64B
f444 cdd0f5    call    0f5d0h		;vypi� HL hexadecim�ln� podle F64B
f447 cd47f6    call    0f647h		;vypi� n�sleduj�c� znak
f44a 20        db      " "		;mezera
f44b 7e        ld      a,(hl)		;vyzvedni znak z dan� adresy
f44c cdd5f5    call    0f5d5h		;vypi� A hexadecim�ln� podle F64B
f44f cd47f6    call    0f647h		;vypi� n�sleduj�c� znak
f452 2d        db      "-"
f453 cd2bf6    call    0f62bh		;vstup z per. podle 2 nejni��. bit� IO (kl�vesnice)
f456 4f        ld      c,a		;uschovej znak do C
f457 79        ld      a,c		;vezmi znak z C
f458 cdb7f4    call    0f4b7h		;otestuj ho
f45b da2bf2    jp      c,0f22bh		;pokud to bylo CR n�vrat do smy�ky monitoru
					;p�es od��dkov�n�
f45e ca7ef4    jp      z,0f47eh		;odsko� pokud to bylo SP (p�esko�en� na dal�� adresu)
f461 fe19      cp      19h		;odsko�, pokud to
f463 ca80f4    jp      z,0f480h		;byla �ipka nahoru
f466 cd4bf6    call    0f64bh		;vypi� dan� znak podle 2 mejni��. bit� IO
f469 e5        push    hl		;uschvej HL (adresu)
f46a 210000    ld      hl,0000h		;po��te�n� hodnota pro vstup ��sla
f46d cdc9f4    call    0f4c9h		;vstup ��sla ze znaku z C a dal��ch z kl�vesnice do HL
f470 7d        ld      a,l		;bereme posledn� 2 ��slice (ni��� bajt HL)
f471 e1        pop     hl		;obnov adresu
f472 77        ld      (hl),a		;ulo� tam danou hodnotu
f473 be        cp      (hl)		;zkontroluj jestli se tam zapsala
f474 c227f2    jp      nz,0f227h	;pokud ne (ROM/m�sto kde nen� RAM), vypi� ?
					;a zp�t do smy�ky monitoru
f477 79        ld      a,c		;do A posledn� stisknut� kl�vesa
f478 fe0d      cp      0dh		;je to CR?
f47a c8        ret     z		;n�vrat pokud ano
f47b c357f4    jp      0f457h		;jinak opakujeme (test kl�vesy na SP/�ipka nahoru)

;zm��knuto SP-p�esko�en� na dal�� adresu
;---------------------------------------
f47e 23        inc     hl		;zvy� adresu
f47f fe        db      0FEh		;finta na p�esko�en� n�sleduj�c�ho bajtu (CP N)
;zm��knuta �ipka nahoru
;----------------------
f480 2b        dec     hl		;sni� adresu (p�esko�eno pokud jdeme p�es SP)
f481 c341f4    jp      0f441h		;a sko� zp�t do p��kazu S na zpracov�n� dal�� adresy


;p��kaz R monitoru - n�vrat
;sko�� na adresu kter� je ulo�ena na 01Dh
;(nap�. tepl� start Basic, nebo Amos)
;========================================
f484 2a1d00    ld      hl,(001dh)	;vyzvedni adresu do HL
f487 e9        jp      (hl)		;a sko� na n�


;v�pis textu z (HL) podle F64B
;posledn� bajt m� 7 bit nastaven na 1
;------------------------------------
f488 7e        ld      a,(hl)		;vyzvedni znak z HL
f489 b7        or      a		;otestuj nejvy��� bit
f48a 4f        ld      c,a		;dej znak do C
f48b fa95f4    jp      m,0f495h		;pokud je to posledn� znak odsko�
f48e cd4bf6    call    0f64bh		;vytiskni znak podle  nejn. 2 bit� IO
f491 23        inc     hl		;zvy� adresu
f492 c388f4    jp      0f488h		;opakuj dokud nejsou v�echny
;posledn� znak
;-------------
f495 e67f      and     7fh		;vynuluj nejvy��� bit
f497 4f        ld      c,a		;dej do C
f498 c34bf6    jp      0f64bh		;a n�vrat p�es v�pis

;zv��en� HL o 1 a test na 0 (Z) plus test HL>DE (CY)
;---------------------------------------------------
f49b 23        inc     hl		;zvy� HL
f49c 7c        ld      a,h		;test na nulu
f49d b5        or      l
f49e 37        scf
f49f c8        ret     z		;n�vrat s nastaven�mi CY a Z pokud je HL 0
					;jinak porovnej DE s HL
;porovn�n� DE a HL (Z p�i nule, CY pokud HL>DE)
;----------------------------------------------
f4a0 7b        ld      a,e		;ode�ti ni��� bajty
f4a1 95        sub     l
f4a2 7a        ld      a,d		;a pak i vy��� i s CY pokud byl nastaven
f4a3 9c        sbc     a,h
f4a4 c9        ret     			;n�vrat

;ASCII znak z A na HEX ��slo (0-F) op�t v A
;pokud to nen� 0-F je nastaveno CY na 1
;==========================================
f4a5 d630      sub     30h		;ode�ti 48 ("0")
f4a7 d8        ret     c		;n�vrat pokud to byl znak s men��m k�dem ne� 48
f4a8 c6e9      add     a,0e9h		;p�i�ti 233
f4aa d8        ret     c		;n�vrat pokud je znak "G" a vy���
f4ab c606      add     a,06h		;p�i�ti 6
f4ad f2b3f4    jp      p,0f4b3h		;odsko� pro A-F
f4b0 c607      add     a,07h		;je to 0-9 ?
f4b2 d8        ret     c		;n�vrat pokud ne
f4b3 c60a      add     a,0ah		;uprav hodnotu na 0-15
f4b5 b7        or      a		;vynuluj CY
f4b6 c9        ret     			;n�vrat

;otestov�n� znaku na SP,CR a ","
;pokud je to jeden z nich je nastaven Z
;pro CR je�t� nav�c CY
;--------------------------------------
f4b7 fe20      cp      20h		;je to SP?
f4b9 c8        ret     z		;n�vrat se Z pokud ano
f4ba fe2c      cp      2ch		;je to ","?
f4bc c8        ret     z		;n�vrat se Z pokud ano
f4bd fe0d      cp      0dh		;je to CR?
f4bf 37        scf     			;nastav CY
f4c0 c8        ret     z		;n�vrat se Z a CY pokud ano
f4c1 3f        ccf     			;vynuluj CY
f4c2 c9        ret     			;n�vrat

;Vstup hexa ��sla z kl�vesnice do HL
;-----------------------------------
f4c3 210000    ld      hl,0000h		;po��te�n� hodnota je 0
f4c6 cd00f0    call    0f000h		;znak z Kl�vesnice vytiskout a do C
					
;Vstup hexa ��sla z C a dal�� z kl�vesnice do HL
;berou se pouze posledn� 4 znaky
;CY pokud je posledn� CR
;-----------------------------------
f4c9 79        ld      a,c		;znak do A
f4ca cda5f4    call    0f4a5h		;ASCII znak z A na ��slo (0-F) op�t v A
f4cd dad9f4    jp      c,0f4d9h		;pokud je to nen� ��seln� znak (0-9,A-F) odsko�
f4d0 29        add     hl,hl		;rotuj HL o 4 bity doprava
f4d1 29        add     hl,hl
f4d2 29        add     hl,hl
f4d3 29        add     hl,hl
f4d4 b5        or      l		;vynuluj CY
f4d5 6f        ld      l,a		;p�idej ��slici do HL na posledn� 4 bity
f4d6 c3c6f4    jp      0f4c6h		;opakuj pro dal�� kl�vesu

;p�i ne��seln� kl�vese
;---------------------
f4d9 79        ld      a,c		;vyzvedni znak do A
f4da cdb7f4    call    0f4b7h		;otestov�n� znaku na SP,CR a ","
f4dd c8        ret     z		;vra� se pokud to byl jeden z nich
f4de c327f2    jp      0f227h		;jinak ? a zp�t do smy�ky monitoru

;vstup B parametr� na z�sobn�k
;vstupn� bod je F4E5h
;-----------------------------
f4e1 f1        pop     af		;obnov AF
f4e2 da27f2    jp      c,0f227h		;ohla� chybu pokud byl CR ale ne v�echny parametry
;vstup zde
;---------
f4e5 cdc3f4    call    0f4c3h		;vstup ��sla do HL z kl�vesnice (CY pokud CR)
f4e8 e3        ex      (sp),hl		;parametr na z�sobn�k
f4e9 e5        push    hl		;a n�vratov� adresa zp�tky taky na z�sobn�k
f4ea f5        push    af		;uschovej AF (CY pokud byla chyba)
f4eb 05        dec     b		;sni� po�et parametr�
f4ec c2e1f4    jp      nz,0f4e1h	;opakuj dokud se nep�evzaly v�echny pot�ebn�
f4ef f1        pop     af		;obnov AF
f4f0 d227f2    jp      nc,0f227h	;pokud nebyl na konci CR ohla� chybu
f4f3 c9        ret     			;n�vrat

;vstup 2 ��slic v ASC k�du z periferie podle IO v F689
;a vytvo�en� 1 bajtu v A (p�ska/mgf)
;-----------------------------------------------------
f4f4 cd0bf5    call    0f50bh		;vstup do A podle F689
f4f7 cda5f4    call    0f4a5h		;p�evod Ascii znaku na ��slo
f4fa 07        rlca    			;rotuj doleva 4x (prvn� ��slice)
f4fb 07        rlca    
f4fc 07        rlca    
f4fd 07        rlca    
f4fe 4f        ld      c,a		;uschovej si prvn� ��st do C
f4ff cd0bf5    call    0f50bh		;vstup do A podle F689
f502 cda5f4    call    0f4a5h		;p�evod Ascii znaku na ��slo
f505 b1        or      c		;p�idej prvn� ��slici
f506 4f        ld      c,a		;v�sledek uschovej do C
f507 82        add     a,d		;p�i�ti k D (kontroln� sou�et)
f508 57        ld      d,a		;ulo� zp�t do D
f509 79        ld      a,c		;v�sledek zp�t do A
f50a c9        ret     			;n�vrat

;vstup 1 bajtu z periferie p�es IO podle F689 do A
;-------------------------------------------------
f50b cd89f6    call    0f689h		;bajt do A
f50e e67f      and     7fh		;nech pouze 7 bit�
f510 c9        ret     			;n�vrat


;Vstup bloku znak� do Mgf. bufferu a vstupn� bod
;pro 1 bajt do A z mgf. p�es buffer je F51Fh
;-----------------------------------------------
f511 77        ld      (hl),a		;ulo� znak do bufferu
f512 2c        inc     l		;zvy� adresu v bufferu
f513 d60d      sub     0dh		;byl znak CR?
f515 c23ef5    jp      nz,0f53eh	;pokud ne sko� na �ten� dal��ho bajtu
f518 6f        ld      l,a		;do L nulu
f519 221900    ld      (0019h),hl	;ulo� adresu bufferu na prom�nnou
f51c c1        pop     bc		;obnov BC
f51d d1        pop     de		;obnov DE
f51e fee5      db      0FEh		;finta na p�esko�en� push (cp 0e5h) pokud jdeme tudy
					;do n�sleduj�c� rutiny

;znak z mgf. do A (p�es buffer !!)
;---------------------------------
f51f e5        push    hl		;uschovej HL (ne pokud p�ich�z� zhora)
f520 2a1900    ld      hl,(0019h)	;vyzvedni adresu bufferu do HL
f523 2c        inc     l		;posu� se na n�sleduj�c� znak
f524 221900    ld      (0019h),hl	;ulo� adresu v bufferu zp�t
f527 ca38f5    jp      z,0f538h		;pokud byl buffer pr�zdn� sko� na na�ten� bloku dat
f52a 2d        dec     l		;sni� zp�t adresu
f52b 7e        ld      a,(hl)		;vyzvedni znak do A
f52c fe0d      cp      0dh		;je to CR?
f52e c236f5    jp      nz,0f536h	;odsko� pokud ne
f531 2eff      ld      l,0ffh		;sign�l pr�zdn� buffer
f533 221900    ld      (0019h),hl	;ulo� na prom�nnou
f536 e1        pop     hl		;obnov HL
f537 c9        ret     			;n�vrat

;byl pr�zdn� buffer mgf, tak�e budem na��tat blok bajt� dokud nebude CR
;----------------------------------------------------------------------
f538 d5        push    de		;uschovej DE
f539 3a1b00    ld      a,(001bh)	;vyzvedni prom�nnou kaze��ku do A (polarita, prodleva)
f53c 57        ld      d,a		;dej do D
f53d c5        push    bc		;uschovej i BC na z�sobn�k
f53e db86      in      a,(86h)		;smy�ka pro nalezen� hrany
f540 aa        xor     d		;se spr�vnou polaritou
f541 f23ef5    jp      p,0f53eh		;ze vstupu magnetofonu (opakuj dokud nen�)
f544 06ff      ld      b,0ffh		;do B po��te�n� hodnota FF (1111 1111b)
f546 1e09      ld      e,09h		;�teme 9 bit� (start + 8 bit�)
f548 db86      in      a,(86h)		;smy�ka pro nalezen�
f54a aa        xor     d		;opa�n� hrany
f54b a8        xor     b		;start bitu
f54c f248f5    jp      p,0f548h		;sign�lu z mgf. (opakuj dokud nen�)
f54f 78        ld      a,b		;vyzvedni aktu�ln� hodnotu tvo�en�ho bajtu
f550 1f        rra     			;p�irotuj z CY dan� bit
f551 1d        dec     e		;sni� po��tadlo bit�
f552 ca11f5    jp      z,0f511h		;pokud hotovo (v�ech 9) odsko� na ulo�en� a test
f555 47        ld      b,a		;jinak ulo� do B akt. hodnotu bajtu
f556 7a        ld      a,d		;vyzvedni si paremetr kaze��ku (prodleva odp. period�)
f557 e67f      and     7fh		;nech jen odpov�daj�c� bity (polarita n�s nezaj�m� te�)
f559 3d        dec     a		;�ekac� smy�ka podle A
f55a c259f5    jp      nz,0f559h	;vytvo�� po�adovanou prodlevu
f55d db86      in      a,(86h)		;na�ti hodnotu ze vstupu magnetofonu
f55f aa        xor     d		;uprav podle polarity
f560 e680      and     80h		;nech jen dan� bit
f562 b0        or      b		;p�idej ho do B k atu�ln� hodnot� bajtu
f563 47        ld      b,a		;ulo� zp�t do B
f564 3c        inc     a		;v p��pad� �e je v A FF
f565 ca46f5    jp      z,0f546h		;do�lo k chyb� a jedem bajt znovu (nebyl start bit
					;nebo stop bit)
f568 c348f5    jp      0f548h		;jinak dal�� bit dokud nejsou v�ecky

;v�stup bajtu z C na magnetofon
;------------------------------
f56b 79        ld      a,c		;znak do A
f56c c5        push    bc		;uschovej BC
f56d f680      or      80h		;nastav nejvy��� bit na 1 (jako stop bit)
f56f 2f        cpl     			;nahra� 1 nulama a naopak
f570 4f        ld      c,a		;uschov�me do C
f571 f3        di      			;zaka� p�eru�en�, je to �asov� kritick� operace
f572 cd93f5    call    0f593h		;nov� perioda 1kHz
f575 3e01      ld      a,01h		;startovac� bit
f577 d387      out     (87h),a		;na bit 0 portu 86 - pomoc� bitov�ho p��stupu p�es 87
f579 0608      ld      b,08h		;8 bit� (po��tadlo v B)
;smy�ka
;------
f57b cd93f5    call    0f593h		;po�k�me 1 periodu
f57e 79        ld      a,c		;do A si vezmeme aktu�ln� hodnotu vys�lan�ho bajtu 
f57f e601      and     01h		;a nech�me pouze 0-t� bit
f581 d387      out     (87h),a		;kter� vy�leme na port 86,0-t� bit (p�es port 87)
f583 79        ld      a,c		;posuneme se na dal�� bit
f584 0f        rrca    			;pomoc� rotace doprava
f585 4f        ld      c,a		;a uschov�me aktu�ln� hodnotu do C
f586 05        dec     b		;sn�en� po��tadla
f587 c27bf5    jp      nz,0f57bh	;a opakov�n� smy�ky dpkod nen� v�ech 8
f58a fb        ei      			;povolen� p�eru�en�
f58b c1        pop     bc		;obnova ulo�en�ho BC
f58c 79        ld      a,c		;a pokud byl vys�lan� bajt
f58d fe0d      cp      0dh		;k�d 0Dh (CR)
f58f cca2f5    call    z,0f5a2h		;tak vytvo� meziblokovou pauzu (dle prom�nn� 001C)
f592 c9        ret     			;n�vrat

;po�kej 1 periodu sign�lu 1kHz z portu 86/bit5
;---------------------------------------------
f593 db86      in      a,(86h)		;na�ti hodnotu
f595 e620      and     20h		;nech p��slu�n� bit
f597 ca93f5    jp      z,0f593h		;dokud je nulov� opakuj (1/2 periody)
f59a db86      in      a,(86h)		;a tot� pro 2 p�l-periodu
f59c e620      and     20h
f59e c29af5    jp      nz,0f59ah	;jen odskok pro 1-�kovou hodnotu
f5a1 c9        ret     			;n�vrat

;meziblokov� pauza po dobu (001C)x20 ms
;(hodnota z adresy 001C)
;--------------------------------------
f5a2 3a1c00    ld      a,(001ch)	;vyzvedni do A p��slu�nou hodnotu

;pauza po dobu Nx20 ms
;N je ��slo z A
;---------------------
f5a5 e5        push    hl		;uschovej HL
f5a6 210800    ld      hl,0008h		;do HL 0008 (nejni��� bajt ��ta�e p�eru�en�)
f5a9 86        add     a,(hl)		;vezmi sou�asnou hodnotu ��ta�e a p�i�ti po�et
					;kter� budeme �ekat
;�ekac� smy�ka
f5aa be        cp      (hl)		;rovn� se nebo je vy��� hodnota ��ta�e t� pot�ebn�?
f5ab f2aaf5    jp      p,0f5aah		;pokud ne �ek�me
f5ae e1        pop     hl		;obnov HL
f5af c9        ret     			;n�vrat

;Od��dkov�n� na periferii ur�en� I/O bajtem v rutin� F64B
;--------------------------------------------------------
f5b0 cd47f6    call    0f647h		;vypi� n�sleduj�c� znak podle F64Bh
f5b3 0d        db      0Dh		;CR
f5b4 cd47f6    call    0f647h		;vypi� n�sleduj�c� znak podle F64Bh
f5b7 0a        db      0Ah		;LF
f5b8 c9        ret     

;Od��dkov�n� na periferii ur�en� I/O bajtem v rutin� F6AB
;--------------------------------------------------------
f5b9 cda7f6    call    0f6a7h		;vypi� n�sleduj�c� znak podle F6ABh
f5bc 0d        db      0Dh		;CR
f5bd cda7f6    call    0f6a7h		;vypi� n�sleduj�c� znak podle F6ABh
f5c0 0a        db      0Ah		;LF
f5c1 c9        ret     			;n�vrat


;p�evod horn�ch 4 bit� registru A (0-F) 
;na 30h-39h (k�d ��slice 0-9) nebo 41h-46h (k�d znaku "A"-"F")
;-------------------------------------------------------------
f5c2 0f        rrca    			;posu� horn� 4 bity na doln�
f5c3 0f        rrca    			
f5c4 0f        rrca    
f5c5 0f        rrca    			;a pokra�uj rutinou

;p�evod doln�ch 4 bit� registru A (0-F) 
;na 30h-39h (k�d znaku "0"-"9") nebo 41h-46h (k�d znaku "A"-"F")
;-------------------------------------------------------------
f5c6 e60f      and     0fh		;a vynuluj horn�
f5c8 c690      add     a,90h		;9-ka do horn�ch 4 bit�
f5ca 27        daa     			;pokud je ve spodn�ch 4 bitech A-F, p�i�te se 6
					;a bude tam 0-5 a nastaven Half-Carry a 9 se zm�n� na A
f5cb ce40      adc     a,40h		;zv���me horn� 4 bity o 4 (bu� 9>D nebo A>E) 
					;a pokud byla zm�na v p�edchoz� instrukci (A-F>0-5)
					;p�i�teme jedni�ku (dostaneme 1-6 na doln�ch 4 bitech)
f5cd 27        daa     			;p�i�te 60h - uprav� horn� 4 bity na 3(z D) nebo 4(z E)
					;a m�me v�sledek 30-39 nebo 41-46
f5ce 4f        ld      c,a		;v�sledek do C
f5cf c9        ret     			;n�vrat

;V�pis HL hexadecim�ln� (4 ��slice) na periferii podle F64B
;----------------------------------------------------------
f5d0 7c        ld      a,h		;nejd��ve H
f5d1 cdd5f5    call    0f5d5h		;p�eve� a vytiskni
f5d4 7d        ld      a,l		;pak L

;V�pis A hexadecim�ln� (2 ��slice) na periferii podle F64B
;----------------------------------------------------------
f5d5 f5        push    af		;uschovej A
f5d6 cdc2f5    call    0f5c2h		;p�eve� horn� 4 bity na znak
f5d9 cd4bf6    call    0f64bh		;a vytiskni ho na danou periferii
f5dc f1        pop     af		;obnov A
f5dd cdc6f5    call    0f5c6h		;p�eve� doln� 4 bity na znak
f5e0 c34bf6    jp      0f64bh		;vytiskni znak a n�vrat


;obsah HL na periferii podle F66B
;--------------------------------
f5e3 7c        ld      a,h		;nejd��v H
f5e4 cde8f5    call    0f5e8h		;a po�li na periferii
f5e7 7d        ld      a,l		;a pak L a n�vrat p�es
;znak z A na periferii podle F66B
;--------------------------------
f5e8 5f        ld      e,a		;uschovej si znak do E
f5e9 cdc2f5    call    0f5c2h		;horn� 4 bity na p�smeno 0-9 nebo A-F
f5ec cd6bf6    call    0f66bh		;na periferii
f5ef 7b        ld      a,e		;obnov znak z E
f5f0 cdc6f5    call    0f5c6h		;doln� 4 bity na p�smeno 0-9 nebo A-F
f5f3 cd6bf6    call    0f66bh		;na periferii
f5f6 7b        ld      a,e		;obnov znak z E	
f5f7 82        add     a,d		;p�i�ti k D kde se po��t� kontr. sou�et
f5f8 57        ld      d,a		;ulo� zp�t do D
f5f9 c9        ret     			;n�vrat

;obsah HL na periferii podle F6AB
;--------------------------------
f5fa 7c        ld      a,h		;nejd��v H
f5fb cdfff5    call    0f5ffh		;a po�li na periferii
f5fe 7d        ld      a,l		;a pak L a n�vrat p�es

;znak z A na periferii podle F6AB
;--------------------------------
f5ff f5        push    af		;uschovej si znak na z�sobn�k
f600 cdc2f5    call    0f5c2h		;horn� 4 bity na p�smeno 0-9 nebo A-F
f603 cdabf6    call    0f6abh		;na periferii
f606 f1        pop     af		;obnov znak
f607 cdc6f5    call    0f5c6h		;doln� 4 bity na p�smeno 0-9 nebo A-F
f60a c3abf6    jp      0f6abh		;na periferii a n�vrat

;ulo�en� registr� na prom�nn� 22-2B
;SP se nastav� na 7FC2 (32kB)
;----------------------------------
f60d 222800    ld      (0028h),hl	;ulo� HL na 0028h
f610 210200    ld      hl,0002h		;SP se zv�t�� o 2
f613 f5        push    af		;AF na z�sobn�k (posune SP o 2)
f614 39        add     hl,sp		;SP+2 do HL
f615 f1        pop     af		;obnov AF
f616 222a00    ld      (002ah),hl	;ulo� SP na 002Ah
f619 e1        pop     hl		;n�vratov� adresa do HL
f61a f3        di			;zaka� p�eru�en�      
f61b 312800    ld      sp,0028h		;nastav adresu pod kterou se ulo�� ostatn� registry
f61e d5        push    de		;na 0026h DE
f61f c5        push    bc		;na 0024h BC
f620 f5        push    af		;na 0022h AF
f621 eb        ex      de,hl		;uschovej si n�vatovou adresu do DE
f622 2a0400    ld      hl,(0004h)	;konec pam�ti do HL
f625 2ec2      ld      l,0c2h		;7FC2 pro 32kB
f627 f9        ld      sp,hl		;nastav SP
f628 fb        ei      			;povol p�eru�en�
f629 eb        ex      de,hl		;vyzvedni si n�vratvou adresu
f62a e9        jp      (hl)		;a vra� se


;vstup bajtu z periferie podle nejni���ch dvou bit� do A
;-------------------------------------------------------
f62b 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f62e e603      and     03h		;nech pouze doln� 2 bity
f630 3ec2      ld      a,0c2h		;nastav A pro adresu 7FC2
f632 caf5f6    jp      z,0f6f5h		;a sko� na ni pokud jsou oba bity 0
f635 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f638 e603      and     03h		;nech pouze doln� 2 bity
f63a 3d        dec     a		;pokud byl bit0 na 1
f63b caaaf8    jp      z,0f8aah		;tak vstup znaku z kl�vesnice s �ek�n�m a p�pnut�m
f63e 3d        dec     a		;pokud to byl bit 1 na 1
f63f caebf6    jp      z,0f6ebh		;tak vystup z d�rn� p�sky
f642 3ec5      ld      a,0c5h		;jinak nastav A pro adresu 7FC5
f644 c3f5f6    jp      0f6f5h		;a sko� na ni (oba bity na 1)

;vypi� znak za CALL na periferii podle F64B
;------------------------------------------
f647 e3        ex      (sp),hl		;vyzvedni adresu znaku a uschovej HL
f648 4e        ld      c,(hl)		;znak do C
f649 23        inc     hl		;zvy� adresu
f64a e3        ex      (sp),hl		;obnov HL a ulo� n�vratovou adresu
					;a pokra�uj p�es F64Bh

;F64Bh v�stup na periferii podle 2 nejni���ch bit� IO
;-----------------------------------------------------
f64b 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f64e e603      and     03h		;nech pouze doln� 2 bity
f650 3ec8      ld      a,0c8h		;nastav A pro adresu 7FC8
f652 caf5f6    jp      z,0f6f5h		;a sko� na ni pokud jsou oba bity 0
f655 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f658 e603      and     03h		;nech pouze doln� 2 bity
f65a 3d        dec     a		;pokud byl pouze bit 0 na 1
f65b ca07f0    jp      z,0f007h		;sko�  na F007h (obrazovka)
f65e 3d        dec     a		;pokud to byl bit 1 na 1
f65f caabf6    jp      z,0f6abh		;sko� na F6AB (tisk podle bit� 6 a 7)
f662 3ecb      ld      a,0cbh		;jinak nastav A pro adresu 7FCBh
f664 c3f5f6    jp      0f6f5h		;a sko� na ni (oba bity na 1)

;vypi� znak za CALL na periferii podle F66B
;------------------------------------------
f667 e3        ex      (sp),hl		;vyzvedni adresu znaku a uschovej HL
f668 4e        ld      c,(hl)		;znak do C
f669 23        inc     hl		;zvy� adresu
f66a e3        ex      (sp),hl		;obnov HL a ulo� n�vratovou adresu
					;a pokra�uj p�es F66Bh

;F66Bh v�stup na periferii podle bit� 4 a 5 IO
;---------------------------------------------
f66b 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f66e e630      and     30h		;nech pouze bity 4 a 5
f670 3ece      ld      a,0ceh		;nastav A pro adresu 7FCEh
f672 caf5f6    jp      z,0f6f5h		;a sko� na ni pokud jsou oba bity 0
f675 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f678 e630      and     30h		;nech pouze bity 4 a 5
f67a fe10      cp      10h		;je to bit 4?
f67c cad9f6    jp      z,0f6d9h		;v�stup na d�rova�
f67f fe20      cp      20h		;je to bit 5?
f681 ca6bf5    jp      z,0f56bh		;v�stup na magnetofon
f684 3ed1      ld      a,0d1h		;jinak nastav A pro adresu 7FD1h
f686 c3f5f6    jp      0f6f5h		;a sko� na ni (oba bity na 1)

;vstup bajtu z periferie podle 2 a 3 bitu do A
;---------------------------------------------
f689 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f68c e60c      and     0ch		;nech pouze 2 a 3 bit
f68e 3ed4      ld      a,0d4h		;nastav A pro adresu 7FD4h
f690 caf5f6    jp      z,0f6f5h		;a sko� na ni pokud jsou oba bity nulov�
f693 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f696 e60c      and     0ch		;nech pouze 2 a 3 bit
f698 fe04      cp      04h		;pokud je 2 bit na 1
f69a caebf6    jp      z,0f6ebh		;pak znak z d�rn� p�sky do A
f69d fe08      cp      08h		;pokud je 3 bit na 1
f69f ca1ff5    jp      z,0f51fh		;znak z mgf. do A (p�es buffer !!)
f6a2 3ed7      ld      a,0d7h		;jinak nastav A pro adresu 7FD7h
f6a4 c3f5f6    jp      0f6f5h		;a sko� na ni (oba bity na 1)

;vypi� znak za CALL na periferii podle F6AB
;------------------------------------------
f6a7 e3        ex      (sp),hl		;vyzvedni adresu znaku a uschovej HL
f6a8 4e        ld      c,(hl)		;znak do C
f6a9 23        inc     hl		;zvy� adresu
f6aa e3        ex      (sp),hl		;obnov HL a ulo� n�vratovou adresu
					;a pokra�uj p�es F6ABh

;F6ABh v�stup na periferii podle bit� 6 a 7 IO
;---------------------------------------------
f6ab 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f6ae e6c0      and     0c0h		;nech pouze 6 a 7 bit
f6b0 3eda      ld      a,0dah		;nastav A pro adresu 7FDAh
f6b2 caf5f6    jp      z,0f6f5h		;a sko� na ni pokud jsou oba bity nulov�
f6b5 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f6b8 e6c0      and     0c0h		;nech pouze 6 a 7 bit
f6ba fe40      cp      40h		;pokud byl bit 6 na 1
f6bc ca4bf6    jp      z,0f64bh		;tak v�sup podle 2 nejni���ch 2 bit� IO
f6bf fe80      cp      80h		;byl pouze bit 7 na 1?
f6c1 3edd      ld      a,0ddh		;nastav A pro adresu 7FDDh	
f6c3 c2f5f6    jp      nz,0f6f5h	;a sko� na ni pokus ne (oba bity na 1)
					;jinak pokra�uj v�stupem na tisk�rnu

;V�stup znaku z C na tisk�rnu
;----------------------------
f6c6 3e0c      ld      a,0ch
f6c8 d3fb      out     (0fbh),a
f6ca 3e0f      ld      a,0fh
f6cc d3fb      out     (0fbh),a
f6ce dbfa      in      a,(0fah)		;nyn� po�kej a� bude tisk�rna
f6d0 e601      and     01h		;p�ipravena p�ijmout data
f6d2 cacef6    jp      z,0f6ceh		;pro tisk (opakuj dokud nen�)
f6d5 79        ld      a,c		;znak z C do A
f6d6 d3f9      out     (0f9h),a		;a na dan� datov� port modulu STAPER (tisk�rna)
f6d8 c9        ret     

;V�stup znaku z C na d�rova�
;---------------------------
f6d9 3e0e      ld      a,0eh
f6db d3fb      out     (0fbh),a
f6dd 3d        dec     a
f6de d3fb      out     (0fbh),a
f6e0 dbfa      in      a,(0fah)		;nyn� po�kej a� bude d�rova�
f6e2 e601      and     01h		;p�ipraven p�ijmout data
f6e4 cae0f6    jp      z,0f6e0h		;pro v�stup (opakuj dokud nen�)
f6e7 79        ld      a,c		;znak z C do A
f6e8 d3f9      out     (0f9h),a		;a na dan� datov� port modulu STAPER (d�rova�)
f6ea c9        ret     			;n�vrat

;Vstup z d�rn� p�sky do registru A
;---------------------------------
f6eb dbfa      in      a,(0fah)		;dokud nejsou data
f6ed e608      and     08h		;p�ipravena
f6ef caebf6    jp      z,0f6ebh		;opakuj test portu
f6f2 dbf8      in      a,(0f8h)		;p�e�ti do A data z d�rn� p�sky
f6f4 c9        ret     			;n�vrat

;skok na adresu 7FXX kde XX je v A
;7F plat� pro 32kB IQ
;---------------------------------
f6f5 e5        push    hl		;uschovej HL na z�sobn�k
f6f6 2a0400    ld      hl,(0004h)	;vyzvedni konec RAM (7F do H)
f6f9 6f        ld      l,a		;ni��� bajt adresy z A do L
f6fa e3        ex      (sp),hl		;obnov HL a dej adresu na z�sobn�k
f6fb c9        ret     			;sko� na ni

;start magnetofonu nebo d�rova�e p�i nahr�v�n�
;---------------------------------------------
f6fc 0603      ld      b,03h		;bit1 portu C-8255 (86) bude na 1 (0xxx001 1)
f6fe 3a0300    ld      a,(0003h)	;vyzvedni IO bajt do A
f701 e630      and     30h		;nech jen p��slu�n� bity
f703 fe10      cp      10h		;je to d�rova�?
f705 4f        ld      c,a		;uschovej A do C
f706 cc3bf7    call    z,0f73bh		;pokud ano sko� na rutinu d�rova�e - vyd�rov�n� 64x0 
f709 79        ld      a,c		;obnov si IO bajt
f70a fe20      cp      20h		;pokud to nen� magnetofon
f70c c0        ret     nz		;tak se vra� (oba bity IO na 1)
f70d 78        ld      a,b		;vyzvedni si hodnotu pro port 86
f70e d387      out     (87h),a		;a nastav p��slu�n� bit (bit1 na 1)
f710 c9        ret     			;n�vrat

;start mgf. a p��prava obrazovky p�i vstupu dat z mgf. do po��ta�e
;-----------------------------------------------------------------
f711 0603      ld      b,03h		;hodnota pro zapnut� mgf. p�es port 87
f713 3a0300    ld      a,(0003h)	;IO bajt do A
f716 e60c      and     0ch		;nech pouze bity 2 a 3
f718 fe08      cp      08h		;pokud nebyl bit 3 na 1
f71a c0        ret     nz		;tak se vra�
f71b 3edf      ld      a,0dfh		;vymaskuj p�eru�en�
f71d d389      out     (89h),a		;na �adi�i 8259 (z�kaz p�eru�en�)
f71f 3a0f00    ld      a,(000fh)	;vyzvedni ��slo ��dku kde je kurzor
f722 d608      sub     08h		;sni� o 8
f724 d230f7    jp      nc,0f730h	;odko� pokud si byl na ��dku 8 a v�ce
f727 0e1a      ld      c,1ah		;jinak vypi� znak
f729 cd07f0    call    0f007h		;kurzor dol� (posune se dol� ��dek)
f72c 3c        inc     a		;a opakuj
f72d c229f7    jp      nz,0f729h	;dokud nebude na ��dku 8
f730 2a2000    ld      hl,(0020h)	;vyzvedni adresu Videoram
f733 2d        dec     l		;sni� o jedna
f734 221900    ld      (0019h),hl	;a nastav tuto adresu jako buffer mgf.
f737 78        ld      a,b		;vyzvedni hodmotu na zapnut� mgf
f738 d387      out     (87h),a		;a po�li ji na dan� port (bit 1 na 1 port C)
f73a c9        ret     			;n�vrat

;v�stup 64 nul na d�rova�
;------------------------
f73b c5        push    bc		;uschovej BC na z�sobn�k
f73c 010040    ld      bc,4000h		;64 v B jako po��tadlo, 0 do C
f73f cdd9f6    call    0f6d9h		;v�stup z C na d�rova�
f742 05        dec     b		;sni� po��tadlo
f743 c23ff7    jp      nz,0f73fh	;opakuj dokud si nevyslal v�echny znaky
f746 c1        pop     bc		;obnov BC
f747 c9        ret     			;n�vrat

;obsluha p�eru�en� 50 Hz
;-----------------------
f748 f5        push    af		;uschovej registry
f749 e5        push    hl
f74a 3e20      ld      a,20h		;povol dal�� p�eru�en� 8259
f74c d388      out     (88h),a
f74e fb        ei      			;povol p�eru�en� procesoru
f74f 210600    ld      hl,0006h		;do HL adresa �asov�n� kl�vesnice a blik�n� kurzoru
f752 7e        ld      a,(hl)		;a jej� obsah do A
f753 a7        and     a		;test na 0
f754 c46cf7    call    nz,0f76ch	;pokud nen� nula nastav adresu 0006
f757 23        inc     hl		;v HL je 0007 - p��znak blik�n� kurzoru
f758 7e        ld      a,(hl)		;dej p��znak do A
f759 23        inc     hl		;posu� HL na 3-bajtov� ��ta� 20 ms
f75a a7        and     a		;pokud A=0
f75b cc84f7    call    z,0f784h		;prove� blik�n� kurzorem
f75e 34        inc     (hl)		;n�sleduje zv��en� 3-bajtov�ho ��ta�e 20ms
f75f c269f7    jp      nz,0f769h	;na adres�ch 0008-000A
f762 23        inc     hl
f763 34        inc     (hl)
f764 c269f7    jp      nz,0f769h
f767 23        inc     hl
f768 34        inc     (hl)
f769 e1        pop     hl		;obnov registry
f76a f1        pop     af
f76b c9        ret     			;a vra� se zp�t z p�eru�en�

;nastaven� adresy 0006 
;d�l� se v p�eru�en� 50Hz pokud je 006 nenulov�
-----------------------------------------------
f76c db85      in      a,(85h)		;test je-li stisknuta
f76e 3c        inc     a		;n�jak� kl�vesa
f76f 7e        ld      a,(hl)		;obsah adresy 0006 do A (�asov�n� kl�vesnice)
f770 ca7cf7    jp      z,0f77ch		;odsko� pokud nen� nic stisknuto
f773 3c        inc     a		;zvy� A
f774 e63f      and     3fh		;ponech doln�ch 6 bit�
f776 77        ld      (hl),a		;ulo� zp�tky na 0006
f777 fe21      cp      21h		;je toto ��slo men�� ne� 21h (33) ?
f779 f8        ret     m		;ano, vra� se
f77a 35        dec     (hl)		;ne, sni� o 1 a
f77b c9        ret     			;n�vrat
;kdy� nen� kl�vesa, v A je obsah 0006
f77c a7        and     a		;nastav p��znaky podle A
f77d 3600      ld      (hl),00h		;vynuluj �asova� kl�vesnice
f77f f8        ret     m		;byl nejvy��� bit 1? pokud ano n�vrat
f780 c680      add     a,80h		;nastav nejvy��� bit na 1 v p�vodn�m obsahu 0006
f782 77        ld      (hl),a		;ulo� na 0006 
f783 c9        ret     			;n�vrat

;blik�n� kurzorem pomoc� p�eru�en� 50Hz
;ka�d� 4 p�eru�en� invertuje znak na pozici kurzoru
;--------------------------------------------------
f784 7e        ld      a,(hl)		;do A obsah 0008 (��ta� 20ms nejni��� bajt)
f785 e603      and     03h		;ponech posledn� 2 bity
f787 c0        ret     nz		;neblikej pokud nejsou 0
f788 e5        push    hl		;uschovej HL
f789 2a0c00    ld      hl,(000ch)	;do HL adresa kurzoru
f78c 7e        ld      a,(hl)		;do A znak kurzoru
f78d ee80      xor     80h		;invertuj nejvy��� bit (invertuj zobrazen� znaku)
f78f 77        ld      (hl),a		;a ulo� zp�t do Videoram
f790 e1        pop     hl		;obnov HL
f791 c9        ret     			;a vra� se


;obsluha p�eru�en� od kl�vesy BR
;-------------------------------
f792 222800    ld      (0028h),hl	;uschovej si HL
f795 e1        pop     hl		;nastav adresu na kterou se bude 
f796 221500    ld      (0015h),hl	;po p�eru�en� vracet na p��slu�nou prom�nnou (0015h)
f799 210400    ld      hl,0004h		;do HL dej adresu kde je prom�nn� konec RAM (0004)
f79c cd13f6    call    0f613h		;uschovej v�echny ostatn� registry
					;a SP nastav pod konec RAM
f79f cd47f6    call    0f647h		;vypi� n�sleduj�c� znak - "#"
f7a2 23        db      "#"
f7a3 2a1500    ld      hl,(0015h)	;vypi� adresu na kterou se bude vracet, t.j.
f7a6 cdd0f5    call    0f5d0h		;kde byl program p�eru�en (v hexa tvaru)
f7a9 3e20      ld      a,20h		;povol dal�� p�eru�en� 8259
f7ab d388      out     (88h),a
f7ad fb        ei      			;a taky procesoru (jinak by nejela kl�vesnice)
f7ae c32bf2    jp      0f22bh		;od��dkuj a sko� do hlavn� smy�ky monitoru


;tabulka hodnot pro inicializaci monitoru
;----------------------------------------
f7b1>12 20     db      020h		;po�et posunovan�ch znak� v p��kazech DC a IL
f7b2>13 1e     db      01Eh		;d�lka str�nky
f7b3>14 02     db      02h		;��dkov�n�
f7b4>15 0000   dw      0000h		;n�vratov� adresa monitoru pro p��kaz R
f7b6>17 04     db      04h		;d�lka t�nu
f7b7>18 12     db      012h		;v��ka t�nu
f7b8>19 00ec   dw      0EC00h		;adresa bufferu pro magnetofon
f7ba>1b 56     db      056h		;nastaven� mgf. (polarita+prodleva)
f7bb>1c 21     db      021h		;meziblokov� mezera na p�sce
f7bc>1d 2bf2   dw      0F22Bh		;adresa tepl�ho startu (zde hl. smy�ka monitoru)
f7be>1f 20     db      020h		;po�et znak� na ��dek
f7bf>20 00ec   dw      0EC00h		;adresa za��tku VIDEORAM
f7c1>22 0000   dw      0000h		;m�sto pro AF
f7c3>24 0000   dw      0000h		;m�sto pro BC
f7c5>26 0000   dw      0000h		;m�sto pro DE
f7c7>28 0000   dw      0000h		;m�sto pro HL
f7c9>2a a07f   dw      07FA0h		;m�sto pro SP
f7cb>2c 0010   dw      01000h		;m�sto pro PC

;odskokov� tabulka pro p�eru�en�
;-------------------------------
f7cd c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FE0 - jp F7EC + FF  - p�er. 0
f7d1 c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FE4 - jp F7EC + FF  - p�er. 1
f7d5 c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FE8 - jp F7EC + FF  - p�er. 2
f7d9 c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FEC - jp F7EC + FF  - p�er. 3
f7dd c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FF0 - jp F7EC + FF  - p�er. 4
f7e1 c392f7ff  db      0c3h,092h,0f7h,0ffh	;>7FF4 - jp F792 + FF  - p�er. 5 od kl�vesy BR
f7e5 c348f7ff  db      0c3h,048h,0f7h,0ffh	;>7FF8 - jp F748 + FF  - p�er. 6 50 Hz
f7e9 c3ecf7    db      0c3h,048h,0f7h		;>7FFC - jp F7EC       - p�er. 7 (16kHz)


;rutina zpracov�vaj�c� standartn� nevyu�it� p�eru�en�
;povol� dal�� p�eru�en� ihned vr�t� zp�t
;-----------------------------------------------------------
f7ec f5        push    af		;uschovej AF
f7ed 3e20      ld      a,20h		;p��kaz konec p�eru�en�
f7ef d388      out     (88h),a		;pro 8259
f7f1 fb        ei      			;povol p�eru�en� procesoru
f7f2 f1        pop     af		;obnov AF
f7f3 c9        ret     			;n�vrat z p�eru�en�

f7f4 ff        db      12x0FFh		;a� do F7FF (nevyu�it� m�sto)


;tato ��st se p�i resetu/startu p�ip�n� na 0000 !!
;0F800h je studen� start syst�mu po resetu/zapnut�
;-------------------------------------------------

f800 c318f8    jp      0f818h		;RESET/Start
f803 c3aaf8    jp      0f8aah		;Znak z kl�vesnice do A s �ek�n�m a p�pnut�m
f806 c3ebf6    jp      0f6ebh		;Vstup z d�rn� p�sky do A
f809 c307f0    jp      0f007h		;znak z C na obrazovku v�etn� ��d�c�ch k�d�
f80c c3d9f6    jp      0f6d9h		;V�stup na d�rova� z C
f80f c3c6f6    jp      0f6c6h		;V�stup na tisk�rnu z C
f812 c31ff5    jp      0f51fh		;Vstup 1 bajtu z magnetofonu do A
f815 c36bf5    jp      0f56bh		;V�stup bajtu z C na magnetofon


;Inicializace po startu pokra�uje zde
;(u� na F818h, ne od nuly)
;------------------------------------
f818 3e01      ld      a,01h		;p�epni EPROM monitoru obvodem 3212
f81a d380      out     (80h),a		;na adresu F800, od 0000 je RAM
f81c 210000    ld      hl,0000h         ;testuj RAM od 0
f81f 24        inc     h		;s krokem 256
f820 7e        ld      a,(hl)
f821 2f        cpl     
f822 77        ld      (hl),a
f823 be        cp      (hl)
f824 2f        cpl     
f825 77        ld      (hl),a
f826 ca1ff8    jp      z,0f81fh		;dokud lze zapsat pokra�uj v testu
f829 2b        dec     hl		;v HL adresa posledn�ho bajtu RAM
f82a 7c        ld      a,h
f82b fee8      cp      0e8h		;bylo to E800 a v�ce?
f82d da32f8    jp      c,0f832h		;pokra�uj pokud ne
f830 26e7      ld      h,0e7h		;maximum E7FF, od E800 je Video64 (+Video32) a Monitor
f832 220400    ld      (0004h),hl	;ulo� do prom�nn� monitoru RAMTOP (0004)
f835 2ec2      ld      l,0c2h		;nastav SP o n�co n�e (7FC2 pro 32 kB RAM)
f837 f9        ld      sp,hl
f838 e5        push    hl		;a ulo� tuto hodnotu na vrchol z�sobn�ku
f839 44        ld      b,h
f83a 0ee0      ld      c,0e0h		;nad z�sobn�kem (od 7FE0) jsou tabulky
f83c 21cdf7    ld      hl,0f7cdh	;skok� na podprogramy obsluhuj�c� p�eru�en�
f83f 11ecf7    ld      de,0f7ech
f842 cd47f2    call    0f247h		;kop�ruj od HL po DE na BC
f845 011200    ld      bc,0012h		;nastav prom�nn� pro monitor (0012-002D)
f848 21b1f7    ld      hl,0f7b1h
f84b 11ccf7    ld      de,0f7cch
f84e cd47f2    call    0f247h		;kop�ruj 
f851 3a00c0    ld      a,(0c000h)	;pokud nen� na (C000) hodnota 3E
f854 fe3e      cp      3eh		;tak nen� p�ipojen modul kresli�e
f856 c281f8    jp      nz,0f881h	;a pokra�uj d�le v inicializaci
f859 3e00      ld      a,00h		;nastaven� kresli�e a jeho prom�nn�ch
f85b 327901    ld      (0179h),a	;prom�nn� kresli�e na adrese 0179 (pis�tko)
f85e d3f4      out     (0f4h),a		;zvedni pis�tko nahoru
f860 3eb6      ld      a,0b6h		;inicializuj kresli�
					;1 01 10 1 10 A,B-mode 1 
					;Port A,B input, C output
f862 d3f3      out     (0f3h),a		;F3 ��d�c� port
f864 af        xor     a		;do A 0
f865 d3f2      out     (0f2h),a		;na port C
f867 3e10      ld      a,10h		;0001 0000
f869 d3f2      out     (0f2h),a		;na port C
f86b 3e32      ld      a,32h		;0 011 001 0 PC1=0
f86d d3f3      out     (0f3h),a		;na ��d�c� port (nuluj bit1 C)
f86f 3e01      ld      a,01h
f871 d3f0      out     (0f0h),a		;??? port A je vstupn� ????
f873 af        xor     a
f874 d3f0      out     (0f0h),a
f876 3e72      ld      a,72h
f878 d3f3      out     (0f3h),a
f87a 3e01      ld      a,01h
f87c d3f1      out     (0f1h),a		;??? port B je vstupn� ????
f87e af        xor     a
f87f d3f1      out     (0f1h),a

f881 210300    ld      hl,0003h		;nastav I/O bajt na standartn� hodnotu
f884 3669      ld      (hl),69h		;69 (v�stup - obrazovka, vstup - kl�vesnice, magnetofon)
f886 2100ec    ld      hl,0ec00h	;do HL adresa RAM Video32 (EC00)
f889 dbfe      in      a,(0feh)		;�ti port FE (Video64)
f88b 3c        inc     a		;a pokud je tam 255
f88c ca96f8    jp      z,0f896h		;nem�me Video64 a sko� d�l na nastaven� Video32
f88f 3e40      ld      a,40h		;pro Video64 je 64 (40h) znak� na ��dek
f891 321f00    ld      (001fh),a	;tak to nastav�me do syst. prom�nn� (001F)
f894 26e8      ld      h,0e8h		;a tak� nastav�me adresu E800
f896 222000    ld      (0020h),hl	;do syst�mov� prom�nn� (0020) adresa Videoram
f899 3eb4      ld      a,0b4h		;inicializace 8255 modulu STAPER
f89b d3fb      out     (0fbh),a		;1 01 10 1 00 A,B-mode 1 
					;Port A input, B,C output
f89d 3e09      ld      a,09h		;0000 1001
f89f d3fb      out     (0fbh),a
f8a1 3e05      ld      a,05h		;0000 0101
f8a3 d3fb      out     (0fbh),a
f8a5 dbf8      in      a,(0f8h)
f8a7 c3b3f1    jp      0f1b3h		;pokra�uj inicializac� �adi�e p�eru�en� 8259

;Znak z kl�vesnice do A s �ek�n�m a p�pnut�m
;-------------------------------------------
f8aa c5        push    bc		;�schova registr�
f8ab e5        push    hl
f8ac 3a0600    ld      a,(0006h)	;do A �asov�n� kl�vesnice a blik�n� kurzoru (0006)
f8af e61f      and     1fh		;nech pouze spodn�ch 5 bit�
f8b1 c2acf8    jp      nz,0f8ach	;a �ekej dokud nejsou nulov� - �ek� ur�enou dobu
f8b4 320700    ld      (0007h),a	;0 na (0007h) povol� blik�n� kurzoru
f8b7 cdc9f8    call    0f8c9h		;Znak z kl�vesnice do C (bez pauzy a bez p�p)
f8ba d2b7f8    jp      nc,0f8b7h	;opakuj dokud se nestiskne kl�vesa
f8bd fe80      cp      80h		;p�pni jinak pro znak 080h (END)
f8bf 210412    ld      hl,1204h
f8c2 ca79f9    jp      z,0f979h		;p�p (parametry v HL)
f8c5 2c        inc     l		;a jinak pro ostatn� kl�vesy
f8c6 c379f9    jp      0f979h		;p�p (parametry v HL)

;Znak z kl�vesnice do C bez �ek�n� (bez p�pnut�)
;-----------------------------------------------
f8c9 f3        di      			;zaka� p�eru�en� - m�n�me nastaven� 8255
f8ca 3e98      ld      a,98h		;po�li 1 00(Amod0) 11, 0(Bmod0) 00 - 
f8cc d387      out     (87h),a		;na ��d�c� port 8255
f8ce db84      in      a,(84h)		;zjisti sloupec stisknut� kl�vesy
f8d0 cd67f9    call    0f967h		;vypo��tej  0-7 nebo nic (pozici prvn�ho nulov�ho bitu)
f8d3 3e8a      ld      a,8ah		;po�li 100 01 0 10 - obnov nastaven� portu 84
f8d5 d387      out     (87h),a		;��d�c� port 8255
f8d7 fb        ei      			;povol p�eru�en�
f8d8 d0        ret     nc		;nen� nic stisknuto - n�vrat
f8d9 65        ld      h,l		;ulo� do H sloupec sti�t�n� kl�vesy (0-7)
f8da db85      in      a,(85h)		;zjisti v kter� �ad� byla sti�t�n� kl�vesa
f8dc cd67f9    call    0f967h		;vypo��tej 0-7 nebo nic
f8df d0        ret     nc		;nen� nic stisknuto - n�vrat
f8e0 7d        ld      a,l		;A=L - �ada v kter� byla sti�t�na kl�vesa
f8e1 87        add     a,a		;n�sob�me 8
f8e2 87        add     a,a		;(posun o 3 bity doleva)
f8e3 87        add     a,a		;
f8e4 b4        or      h		;ORem p�id�me sloupec (spodn� 3 bity)
					;tzn. 8x �ada + sloupec
f8e5 211ef9    ld      hl,0f91eh	;tudy se bude vracet
f8e8 e5        push    hl		;(na z�sobn�k)
f8e9 2127f9    ld      hl,0f927h	;tabulka  jednotliv�ch k�d� kl�vesnice v 8-c�ch
f8ec 85        add     a,l		;vypo��tej adresu znaku
f8ed 6f        ld      l,a		;do HL
f8ee 4e        ld      c,(hl)		;a do C vyzvedni k�d sti�t�n�ho znaku
f8ef 79        ld      a,c		;n�sleduje p�r test� - k�d do A
f8f0 fe18      cp      18h		;byl to 18 - kurzor vpravo ?
f8f2 c2fbf8    jp      nz,0f8fbh	;pokud ne pokra�uj
f8f5 3a0b00    ld      a,(000bh)	;pokud ano, dej do A k�d znaku na kter� ukazuje kurzor
f8f8 c31df9    jp      0f91dh		;a pokra�uj n�vratem s t�m �e tenhle znak bude v C

;vyhodnocen� CTRL A SH
;---------------------
f8fb fe21      cp      21h		;pokud je to znak men�� ne� 21h
f8fd d8        ret     c		;tak se vra� p�es 0F91E
f8fe db86      in      a,(86h)		;port C - horn� 4 bity jsou tla��tka FB FB CTRL SH
f900 2f        cpl     			;invertuj (1 pokud bylo sti�t�no)
f901 e630      and     30h		;zaj�m� n�s pouze CTRL a SH
f903 c8        ret     z		;pokud nebylo vra� se p�ez 0F91E
f904 e610      and     10h		;je to SH?
f906 ca17f9    jp      z,0f917h		;sko� pokud to byl CTRL
;stisknuta kl�vesa a SH
;------------------------
f909 67        ld      h,a		;schovej si A (hodnota 0001 0000)
f90a 79        ld      a,c		;vyzvedni si k�d znaku
f90b fe40      cp      40h		;pro znaky 33-63 se bude Xorovat (m�nit) bit 4
					;9=) - horn� znaky nad nep�smenkama
f90d 7c        ld      a,h		;obnov schovanou hodnotu
f90e da12f9    jp      c,0f912h		;odsko� pro 33-63
f911 87        add     a,a		;pro znaky 64 a v��e se m�n� bit 5
					;z velk�ch ud�l� mal� p�smenka
f912 a9        xor     c		;prove� zm�nu
f913 4f        ld      c,a		;ulo� do C
f914 3e80      ld      a,80h		;v A 80 jako p��znak k�du se shiftem
f916 c9        ret     			;a vra� se p�es 0F91E
;stisknuta kl�vesa a CTRL
;------------------------
f917 79        ld      a,c		;vyzvedni si k�d znaku
f918 fe40      cp      40h		;pro znaky 33-63 se
f91a d8        ret     c		;vra� p�es 0F91E
f91b e61f      and     1fh		;jinak ponech pouze spodn�ch 5 bit� (rozsah 0-31)
f91d 4f        ld      c,a		;vlo� do C a vra� se p�es
;tudy se vrac� rutina Znak z kl�vesnice do C bez �ek�n�
;provede nastaven� �asov�n� kl�vesnice a blik�n� kurzoru
;-------------------------------------------------------
f91e 210600    ld      hl,0006h		;HL = prom�nn� - �asov�n� kl�vesnice a blik�n� kurzoru
f921 35        dec     (hl)		;testujeme jestli je 0
f922 34        inc     (hl)		;
f923 37        scf     			;nastav Carry 
f924 c0        ret     nz		;pro n�vrat pokud nen� 0
f925 34        inc     (hl)		;zvy� prom�nnou (sma� carry)
f926 c9        ret     			;vra� se

;tabulka jednotliv�ch k�d� kl�vesnice v 8-c�ch
----------------------------------------------
f927 313233    db      "12345678"
f92a 343536
f92d 3738
f92f 515745    db      "QWERTYUI"
f932 525459
f935 5549
f937 415344    db      "ASDFGHJK"
f93a 464748
f93d 4a4b
f93f 5a5843    db      "ZXCVBNM,"
f942 56424e
f945 4d2c
f947 394f4c    db      "9OL.",0dH,01DH,01CH,1
f94a 2e0d1d
f94d 1c01
f94f 30503b    db      "0P;/\",01eH,8,2
f952 2f5c1e
f955 0802
f957 5e5b5d    db      "^[]",01AH,5,0BH,0CH,4
f95a 1a050b
f95d 0c04
f95f 2d403a    db      "-@: _",019H,018H,3
f962 205f19
f965 1803

;rutina najde prvn� nenulov� bit v A
;jeho ��slo (0-7) je v L
;pokud najde je nastaven carry
;-----------------------------------
f967 2e08      ld      l,08h		;jedeme po 8 bit� 
f969 2d        dec     l		;posu� se na dal�� bit
f96a 3f        ccf     			;invertuj carry
f96b f8        ret     m		;n�vrat p�i L=255 - nenastaven ��dn� bit na 0
f96c 07        rlca    			;posun na dal�� bit
f96d da69f9    jp      c,0f969h		;pokud je jedni�kov� opakuj
f970 c602      add     a,02h		;???
f972 c9        ret     			;n�vrat

;p�pnut� s d�lkou 18 a v��kou 17
--------------------------------
f973 c5        push    bc		;uschovej registry
f974 e5        push    hl
f975 f3        di      			;zaka� p�eru�en�
f976 2a1700    ld      hl,(0017h)	;v��ka a d�lka t�nu do HL
f979 d5        push    de		;uschovej i DE
f97a 5d        ld      e,l		;v��ka do E (z L)
f97b 3e06      ld      a,06h		;0000 0110
;smy�ka p�ep�naj�c� v�stup na repro
f97d 53        ld      d,e		;v��ku do D z E kde byla uschovan� (doba mezi p�epnut�m)
f97e d387      out     (87h),a		;0000 011 0 v�stup na repro - d� tam 0 (bit3 C)
					;p�i prvn�m pr�chodu, pak se st��d� 1-0-1-0-...
f980 2b        dec     hl		;256*h je doba trv�n� t�nu (l zanedb�v�
f981 25        dec     h		;otestuj H na nulu
f982 24        inc     h
f983 ca8ff9    jp      z,0f98fh		;pokud ano kon��me
f986 15        dec     d		;��ta� pro v��ku (pauzy mezi p�epnut�m z 0 na 1)
f987 c280f9    jp      nz,0f980h	;opakuj - �ek�n� mezi p�enut�m z 0-1 a naopak
f98a ee01      xor     01h		;v�stup na repro bude invertov�n (0-1 nebo 1-0)
f98c c37df9    jp      0f97dh		;a zp�t do smy�ky generuj�c� p�pnut�
;ukon�en� p�p	
f98f d1        pop     de		;obnov registry
f990 e1        pop     hl
f991 79        ld      a,c		;tato rutina nem�n� obsah BC ...
					;tak�e znak co byl v C do A
f992 c1        pop     bc
f993 fb        ei      			;povol p�eru�en�
f994 c9        ret     			;a n�vrat

;rolov�n� textu na obrazovku je-li ��slo zobrazovan�ho ��dku
;rovno ��slu na 0013 (d�lka str�nky)
;-----------------------------------------------------------
f995 c5        push    bc		;uschovej registry BC a DE
f996 d5        push    de
f997 2a2000    ld      hl,(0020h)	;do HL adresa za��tku VIDEORAM
f99a 5d        ld      e,l		;a z�rove� i do DE
f99b 54        ld      d,h		;v DE adresa prvn�ho ��dku obrazovky
f99c 3a1f00    ld      a,(001fh)	;do A po�et znak� na ��dek (32/64 standartn�)
f99f 6f        ld      l,a		;a d�me ho do L - v HL adresa 2 ��dku obrazovky
f9a0 3a1300    ld      a,(0013h)	;do A d�lka str�nky (po�et ��dk�: 1-31)
f9a3 4f        ld      c,a		;d�me do C
f9a4 3a1f00    ld      a,(001fh)	;do A po�et znak� na ��dek
f9a7 47        ld      b,a		;d�me do B
f9a8 7e        ld      a,(hl)		;zkop�ruj znak z HL
f9a9 12        ld      (de),a		;na DE
f9aa 13        inc     de		;zvy� adresy
f9ab 23        inc     hl		;(posun na dal�� znak)
f9ac 05        dec     b		;sni� po��tadlo znak� na ��dek
f9ad c2a8f9    jp      nz,0f9a8h	;a opakuj kop�rov�n� dokud ��dek neskon�il
f9b0 0d        dec     c		;sni� po��tadlo ��dk�
f9b1 c2a4f9    jp      nz,0f9a4h	;opakuj dokud nejsou p�esunuty v�echny ��dky
f9b4 3a1f00    ld      a,(001fh)	;do A po�et znak� na ��dek
f9b7 4f        ld      c,a		;do C
f9b8 3e20      ld      a,20h		;do A mezera
f9ba 12        ld      (de),a		;vypl� posledn� ��dek obrazovky mezerama
f9bb 13        inc     de		;posun na dal�� adresu
f9bc 0d        dec     c		;dokud nen� cel� ��dek
f9bd c2baf9    jp      nz,0f9bah	;opakuj
f9c0 d1        pop     de		;obnov registry
f9c1 c1        pop     bc
f9c2 c9        ret     			;a n�vrat

f9c3           db      1596x0FFh	;a� do konce pam�ti (FFFF) jsou sam� FF (voln� m�sto)
