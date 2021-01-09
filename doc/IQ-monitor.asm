;IQ 151 MONITOR
;==============
;
;Start je na adrese 0F800h, která je po startu IQ HW pøepnuta i na 0000h
;
;
;Systémové promìnné Monitoru:
;============================
;0000-0002 - volné, mono vyuít pøi RST 0
;0003      - I/O bajt (69 standartnì)
;0004-0005 - konec RAM (7FFF pro 32kB)
;0006      - èasování klávesnice a blikání kurzoru
;0007      - 0=kurzor bliká, jinak blokováno
;0008-000A - èítaè 20 ms; zastaveno pøi zamaskovaném pøerušení 50 Hz 8259
;000B      - kód znaku na kterı ukazuje kurzor
;000C-000D - adresa místa na obrazovce na které ukazuje kurzor ve Videoram
;000E      - sloupec, ve kterém je právì kurzor (00-1F - 00-3F pro Video 64)
;000F      - øádek, ve kterém je právì kurzor
;0010      - 1=grafické znaky, 0=normální reim
;0011      - 1=znaky v inverzi, 0=normální reim
;0012      - poèet posunovanıch znakù v pøíkazech DC a IL
;0013      - délka stránky (01-1F)
;0014      - øádkování (o kolik dolù se posune kurzor pøi odøádkování CR - znak 0D)
;0015-0016 - návratová adresa monitoru pro pøíkaz R
;0017-0018 - vıška a délka tónu pro rutinu F973
;0019-001A - adresa bufferu pro magnetofon
;001B      - bit 7 urèuje polaritu signálu na pásce (standartnì 0), ostatní bity jsou èíslo
; 	     úmìrné délce periody modulovaného kmitoètu (1kHz)
;001C      - èas pro èekací smyèku F5A2 (mezi bloky na pásce)
;001D-001E - adresa teplého startu (Basicu pøípadnì jiného jazyka podle pøipojeného modulu)
;001F      - poèet znakù na øádek - 20/40h Video32/64
;0020-0021 - adresa zaèátku VIDEORAM EC00 pro Video32, E800 pro Video64
;0022-002D - adresa, do které se ukládají hodnoty registrù procesoru
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


;Komentovanı vıpis:
===================


;znak z klávesnice na obrazovku (I/O=69)
;nebo na periferii podle I/O bajtu (F64B)
;----------------------------------------
f000 cd2bf6    call    0f62bh		;vyzvedni znak z klávesnice (periferie)

;ASCII znak na periferii podle I/O bajtu
;podle rutiny F6B4, vstup v reg. A
;---------------------------------------
f003 4f        ld      c,a		;dej znak do C
f004 c34bf6    jp      0f64bh		;tisk podle I/O bajtu (podle F64B)


;znak v ASCII kódu z reg. C obrazovku
;vèetnì øídících znakù
-------------------------------------
f007 f5        push    af		;uschovej registry
f008 c5        push    bc
f009 d5        push    de
f00a e5        push    hl
f00b 2190f1    ld      hl,0f190h	;0F190h je adresa pøes kterou se bude rutina	 							;vracet a konèit
f00e e5        push    hl		;dej ji na zásobník 
f00f 210700    ld      hl,0007h
f012 75        ld      (hl),l		;vynuluje obsah adresy 0007 - povolí blikání kurzoru
f013 3a0b00    ld      a,(000bh)	;vlo do A kód znaku na kterı ukazuje kurzor
f016 2a0c00    ld      hl,(000ch)	;do HL dej adresu kurzoru ve Videoram
f019 77        ld      (hl),a		;vlo na ni ten znak
f01a 111000    ld      de,0010h
f01d 79        ld      a,c		;do A znak kterı bude rutina F007 tisknout
f01e fe20      cp      20h		;je to øídící kód (<32) nebo ne?
f020 f25bf1    jp      p,0f15bh		;tisk znaku s kódem 32 a vyšším, jinak øídící kód 0-31
f023 d607      sub     07h		;nezpracovávej kódy 0-6
f025 d8        ret     c		;je to 0-6 take návrat
f026 2139f0    ld      hl,0f039h	;tabulka s offsety øídících kódù
f029 85        add     a,l		;najdi odpovídající pozici v tabulce
f02a 6f        ld      l,a		;adresa je teï v HL
f02b d5        push    de		;uschovej DE
f02c 5e        ld      e,(hl)		;vyzvedni z tabulky offset adresy
f02d 1600      ld      d,00h		;je to jen 1 bajt (všechny rutiny jsou v rozsahu
f02f 2152f0    ld      hl,0f052h	;255 bajtù od 0F052)
f032 19        add     hl,de		;vypoèti adresu rutiny øídícího znaku 7-31
f033 d1        pop     de		;obnov DE
f034 e5        push    hl		;adresa rutiny na zásobník
f035 210e00    ld      hl,000eh		;do HL 000Eh - adresa sloupce kurzoru - pro kód 0D
f038 c9        ret     			;skoè na pøíslušnou rutinu

;tabulka s offsety øídících kódù
;-------------------------------
f039 1a        db      01Ah		;F06Ch => kód 07 - pípnutí (je na F052+1A)
f03a 1d        db      01Dh		;F06Fh => kód 08 - kurzor vlevo
f03b 61        db      061h		;F0B3h => kód 09 - tabulátor 8 znakù
f03c 19        db      019h		;F06Bh => kód 0A - pouze RET (ten nejblií :) )
f03d 19        db      019h		;F06Bh => kód 0B - pouze RET
f03e 13        db      013h		;F065h => kód 0C - kurzor na pozici 0,0 (HOME)
f03f e5        db      0E5h		;F137h => kód 0D - odøádkování a zrušení grafického
					;                  i inverzního reimu (CR)
f040 b2        db      0B2h		;F104h => kód 0E - pøepnutí z grafiky
					;                  do normálního reimu
f041 b2        db      0B2h		;F104h => kód 0F - pøepnutí do grafického reimu
f042 19        db      019h		;F06Bh => kód 10 - pouze RET
f043 19        db      019h		;F06Bh => kód 11 - pouze RET
f044 b1        db      0B1h		;F103h => kód 12 - pøepnutí z inverze
					;                  do normálního reimu
f045 b1        db      0B1h		;F103h => kód 13 - pøepnutí do inverzního reimu
f046 19        db      019h		;F06Bh => kód 14 - pouze RET
f047 19        db      019h		;F06Bh => kód 15 - pouze RET 
f048 19        db      019h		;F06Bh => kód 16 - pouze RET
f049 19        db      019h		;F06Bh => kód 17 - pouze RET 
f04a 4c        db      04Ch		;F09Eh => kód 18 - kurzor vpravo
f04b 53        db      053h		;F0A5h => kód 19 - kurzor nahoru
f04c 59        db      059h		;F0ABh => kód 1A - kurzor dolù
f04d 19        db      019h		;F06Bh => kód 1B - pouze RET 
f04e 6e        db      06Eh		;F0C0h => kód 1C - IC (Insert Column) - vytvoøení mezery
					;mezi znaky na místì kurzoru a posun N znakù,
					;N je na adrese 0012
					;posun se zastavuje také na místì kde je znak 0D
f04f b7        db      0B7h		;F109h => kód 1D - DC (Delete Column) - smazání znaku
                                        ;na místì kurzoru s posuvem
f050 19        db      019h		;F06Bh => kód 1E - pouze RET
f051 00        db      0		;F052h => kód 1F - rutina mazání obrazovky (pomocí " ")


;tisk øídícího kódu 1Fh - mazání obrazovky
;-----------------------------------------
f052 2a2000    ld      hl,(0020h)	;do HL adresa zaèátku VIDEORAM
f055 3a1f00    ld      a,(001fh)	;do A poèet znakù na øádek
f058 0620      ld      b,20h		;do B 32 - obrazovka má 32 øádkù
f05a 3620      ld      (hl),20h		;vlo znak " " (mezera) do VIDEORAM (HL)
f05c 23        inc     hl		;dvì smyèky pro AxB znakù
f05d 05        dec     b		
f05e c25af0    jp      nz,0f05ah
f061 3d        dec     a
f062 c258f0    jp      nz,0f058h	;opakuj do konce obrazovky a
					;pokraèuj nastavením pozice kurzoru

;tisk øídícího kódu 0Ch - kurzor na pozici 0,0 (HOME)
;----------------------------------------------------
f065 210000    ld      hl,0000h		;do HL 0
f068 220e00    ld      (000eh),hl	;vlo na 000E a 000F - øádek a sloupec kurzoru
f06b c9        ret     			;návrat

;tisk øídícího kódu 07 - pípnutí
;-------------------------------
f06c c373f9    jp      0f973h		;skoè na vlastní rutinu na F973

;tisk øídícího kódu 08 - kurzor vlevo
;------------------------------------
f06f 2a0c00    ld      hl,(000ch)	;do HL adresa místa na obrazovce
f072 2b        dec     hl		;sni ji (doleva)
f073 3a2100    ld      a,(0021h)	;vyšší bajt adrey zaèátku Videoram
f076 3d        dec     a		;sni
f077 bc        cp      h		;porovnej s H
f078 c8        ret     z		;nedìlej nic a vra se pokud si mimo Videoram
f079 3a1f00    ld      a,(001fh)	;do A poèet znakù na øádek
f07c 4f        ld      c,a		;dej do C
f07d 3d        dec     a		;sni o 1
f07e a5        and     l		;vymaskuj adresou
f07f 320e00    ld      (000eh),a	;nastav aktuální sloupec
f082 79        ld      a,c		;poèet znakù na øádce zpìt do A
f083 fe40      cp      40h		;je to 64?
f085 ca89f0    jp      z,0f089h		;pokud ano poposkoè kousek
f088 29        add     hl,hl		;HLx2
f089 29        add     hl,hl		;HLx4 
f08a 29        add     hl,hl		;HLx8 (x4 pro V64)
f08b 7c        ld      a,h		;vyšší bajt do A
f08c e61f      and     1fh		;nech pouze 5 bitù které tvoøí aktuální øádek
f08e 320f00    ld      (000fh),a	;a ulo na potøièné místo (aktuální øádek) 
f091 67        ld      h,a		;ulo si øádek do H
f092 3a1300    ld      a,(0013h)	;délka stránky do A
f095 3c        inc     a		;zvyš o 1
f096 bc        cp      h		;porovnej s aktuálním øádkem
f097 c0        ret     nz		;hotovo pokud se nerovnají
f098 2a0c00    ld      hl,(000ch)	;vyzvedni znovu adresu místa v obrazovce do HL
f09b c379f0    jp      0f079h		;jsme v pravém dolním rohu tak opìt nastav promìnné
					;(ádnı posun)

;tisk øídícího kódu 18h - kurzor vpravo
;--------------------------------------
f09e 2a0c00    ld      hl,(000ch)	;do HL adresa místa na obrazovce
f0a1 23        inc     hl		;zvyš ji (doprava)
f0a2 c379f0    jp      0f079h		;pokraèuj v rutinì Doleva

;tisk øídícího kódu 19h - kurzor nahoru
;--------------------------------------
f0a5 23        inc     hl		;zvyš HL na 0Fh (øádek kde je kurzor)
f0a6 7e        ld      a,(hl)		;aktuální øádek do A
f0a7 3d        dec     a		;sni hodnotu o 1 (nahoru)
f0a8 f8        ret     m		;nedìlìj nic pokud si pøijel na 255 (byla 0)
f0a9 77        ld      (hl),a		;ulo sníenou hodnotu èísla akt. øádku
f0aa c9        ret     			;návrat

;tisk øídícího kódu 1Ah - kurzor dolù
;------------------------------------
f0ab 23        inc     hl		;zvyš HL na 0Fh (øádek kde je kurzor)
f0ac 3a1300    ld      a,(0013h)	;délka stránky do A
f0af be        cp      (hl)		;porovnej s aktuálním øádkem
f0b0 c8        ret     z		;pokud si na posledním tak návrat
f0b1 34        inc     (hl)		;jinak zvyš èíslo aktuálního øádku
f0b2 c9        ret     			;a návrat

;tisk øídícího kódu 09 - tabulátor 8 znakù
;-----------------------------------------
f0b3 7e        ld      a,(hl)		;vyzvedni hodnotu aktuálního sloupce
f0b4 e6f8      and     0f8h		;vynuluj spodní 3 bity (jedeme po 8)
f0b6 c608      add     a,08h		;pøièti 8
f0b8 4f        ld      c,a		;ulo si novou hodnotu do C
f0b9 3a1f00    ld      a,(001fh)	;do A poèet znakù na øádek
f0bc 3d        dec     a		;sni hodnotu (maska)
f0bd a1        and     c		;nech pouze danı poèet bitù sloupce (nepøekroèí šíøku)
f0be 77        ld      (hl),a		;ulo novou hodnotu sloupce
f0bf c9        ret     			;návrat

;tisk øídícího kódu 1Ch - IC
;(Insert Column) - vytvoøení mezery mezi znaky na místì kurzoru
;a posun N znakù, N je na adrese 0012
;posun se zastavuje také na místì kde je znak 0D
;--------------------------------------------------------------
f0c0 0620      ld      b,20h		;" " do B
f0c2 3a1200    ld      a,(0012h)	;poèet posunovanıch znakù do A
f0c5 4f        ld      c,a		;a hned do C
f0c6 7e        ld      a,(hl)		;vyzvedni hodnotu aktuálního sloupce
f0c7 23        inc     hl		;posuò se na adresu promìnné øádek kde je kurzor
f0c8 56        ld      d,(hl)		;aktuální øádek do D
f0c9 2a0c00    ld      hl,(000ch)	;do HL aktuální adresa na obrazovce
;smyèka podle C (poèet posunovanıch znakù)
;-----------------------------------------
f0cc 0d        dec     c		;pokud není co posunovat (poèet je nula)
f0cd f8        ret     m		;návrat
f0ce e5        push    hl		;uschovej si adresu na zásobník
f0cf 3c        inc     a		;zvyš sloupec o jedna
f0d0 211f00    ld      hl,001fh		;do HL adresa poètu znakù na øádek
f0d3 be        cp      (hl)		;porovnej se sloupcem
f0d4 c2f3f0    jp      nz,0f0f3h	;pokud se nerovnají poskoè dopøedu
					;jinak posun na novı øádek
f0d7 3a1300    ld      a,(0013h)	;do A délka stránky
f0da ba        cp      d		;jsi na poslední øádku?
f0db c2f2f0    jp      nz,0f0f2h	;skoè dopøedu pokud ne
f0de cd95f9    call    0f995h		;roluj obrazovku
f0e1 210f00    ld      hl,000fh		;sni hodnutu
f0e4 35        dec     (hl)		;aktuálního øádku (po odrolování)
f0e5 00        nop     
f0e6 e1        pop     hl		;vyzvedni adresu ve videoram ze zásobníku
f0e7 c5        push    bc		;uschovej poèítadlo
f0e8 3a1f00    ld      a,(001fh)	;vıpoèet adresy ve videoram
f0eb 2f        cpl     			;kde budem pokraèovat
f0ec 4f        ld      c,a		;v posunování po odrolování
f0ed 06ff      ld      b,0ffh		;(o øádek vıš)
f0ef 09        add     hl,bc		;odeèti od HL délku øádku
f0f0 c1        pop     bc		;obnov poèítadlo
f0f1 e5        push    hl		;ulo adresu na zásobník
f0f2 af        xor     a		;do A 0 (jsi na novém øádku, sloupec nula)
f0f3 e1        pop     hl		;obnov adresu ve videoram
f0f4 5e        ld      e,(hl)		;vezmi souèasnou hodnotu
f0f5 70        ld      (hl),b		;vlo na její místo mezeru
f0f6 c5        push    bc		;uschovej poèítadlo
f0f7 4f        ld      c,a		;ulo si do C aktuální sloupec
f0f8 78        ld      a,b		;znak kterı jsme vkládali do A
f0f9 fe0d      cp      0dh		;byl to CR?
f0fb 79        ld      a,c		;do A aktuální sloupec
f0fc c1        pop     bc		;vyzvedni poèítadlo
f0fd c8        ret     z		;návrat byl-li tam znak CR
f0fe 43        ld      b,e		;do B dej znak co byl na posunované pozici
					;pro pøíští vkládání
f0ff 23        inc     hl		;zvyš adresu ve videoram a 
f100 c3ccf0    jp      0f0cch		;opakuj dokud nejsou posunuté všechny znaky

;tisk øídícího kódu 12/13 - pøepnutí z/do inverzního reimu
;-----------------------------------------------------
f103 13        inc     de		;zvyš de na 011h
f104 79        ld      a,c		;kód do A
f105 e601      and     01h		;nech pouze 0-tı bit (0=vypnuto/1=zapnuto)
f107 12        ld      (de),a		;ulo na patøiènou promìnnou (011h)
f108 c9        ret     			;návrat

;tisk øídícího kódu 1Dh - DC
;(Delete Column) - smazání znaku na místì kurzoru s posuvem
;----------------------------------------------------------
f109 3a1200    ld      a,(0012h)	;poèet posunovanıch znakù do A
f10c 4f        ld      c,a		;a jako poèítadlo do C
f10d 0d        dec     c		;sni poèítadlo o 1
f10e 46        ld      b,(hl)		;do B èíslo sloupce kde je kurzor (z 0Eh)
f10f 23        inc     hl		;posuò HL na promìnnou èíslo øádku
f110 56        ld      d,(hl)		;vyzvedni hodnotu akt. øádku do D
f111 2a0c00    ld      hl,(000ch)	;do HL akt. adresa ve Videoram
f114 7e        ld      a,(hl)		;vyzvedni znak z daného místa
f115 fe0d      cp      0dh		;je to CR?
f117 c8        ret     z		;pokud ano není co mazat a návrat
f118 0d        dec     c		;sni poèítadlo
f119 fa34f1    jp      m,0f134h		;pokud bylo 0 odskoè (hotovo)
f11c 23        inc     hl		;zvyš adresu
f11d 04        inc     b		;zvyš èíslo sloupce
f11e 3a1f00    ld      a,(001fh)	;poèet znakù v øádce do A
f121 b8        cp      b		;porovnej s akt. sloupcem
f122 c22df1    jp      nz,0f12dh	;pokud nejsi na konci øádku poskoè
f125 3a1300    ld      a,(0013h)	;délka stránky do A
f128 ba        cp      d		;porovnej s akt. øádkem
f129 c8        ret     z		;pokud jsi na posledním øádku okamitı návrat
f12a 0600      ld      b,00h		;jinak nastav sloupec na 0
f12c 14        inc     d		;posuò se na novı øádek
f12d 7e        ld      a,(hl)		;vyzvedni znak z Videoram
f12e 2b        dec     hl		;sni adresu (posun doleva)
f12f 77        ld      (hl),a		;vlo tam danı znak
f130 23        inc     hl		;zvyš adresu
f131 c315f1    jp      0f115h		;opakuj dokud nejsou smazány všechyn potøebné znaky
;poèítadlo bylo 0, hotovo
;------------------------
f134 360d      ld      (hl),0dh		;vlo do Videoram na akt. pozici CR
f136 c9        ret     			;návrat

;tisk øídícího kódu 0Dh - CR
;odøádkování a zrušení grafického i inverzního reimu
;----------------------------------------------------
f137 3600      ld      (hl),00h		;nastav aktuální sloupec na 0
f139 210000    ld      hl,0000h		;nastav reim na normální
f13c 221000    ld      (0010h),hl	;(není inverze ani grafika)
f13f 2a0c00    ld      hl,(000ch)	;aktuální adresa ve videoram do HL
f142 71        ld      (hl),c		;vlo tam kód CR
f143 3a1400    ld      a,(0014h)	;do A hodnota øádkování
f146 4f        ld      c,a		;schovej si ji do C jako poèítadlo
f147 210f00    ld      hl,000fh		;adresa promìnné øádek kde je kurzor do HL
f14a 3a1300    ld      a,(0013h)	;délka stránky do A
f14d be        cp      (hl)		;porovnej aktuální øádku s délkou stránky
f14e c255f1    jp      nz,0f155h	;pokud nejsi na poslední øádce poskoè
f151 cd95f9    call    0f995h		;odroluj obrazovku
f154 fe        db      0FEh		;fígl k pøeskoèení následující instrukce (cp N)
					;tzn. neprovede se inkrementace pokud bylo Call F995
f155 34        inc     (hl)		;zvyš èíslo akt. øádku je-li to potøeba (ne pokud
					;se rolovalo)
f156 0d        dec     c		;sni poèítadlo øádkování
f157 c247f1    jp      nz,0f147h	;opakuj dokud není odøádkován danı poèet øádkù
f15a c9        ret     			;návrat

;pokraèování F007 - tisk znaku s kódem 32 a vyšším
;-------------------------------------------------
f15b eb        ex      de,hl		;HL=010h, DE=0Eh
f15c 7e        ld      a,(hl)		;vyzvedni promìnnou graf./norm. reim
f15d e601      and     01h		;otestuj jakı je reim
f15f ca6af1    jp      z,0f16ah		;odskoè pro normální reim
					;jinak grafickı reim
f162 79        ld      a,c		;do A znak co se bude tiskout
f163 d640      sub     40h		;odeèti 64
f165 f8        ret     m		;pokud to bylo 32-63 návrat 
f166 fe20      cp      20h		;máme jen 31 gr. znakù
f168 f0        ret     p		;pokud je to 32 a víc opìt návrat bez tisku
f169 4f        ld      c,a		;dej znak do C
;normální reim (grafické znaky 64-95 pøekódovány na 0-31)
;---------------------------------------------------------
f16a 23        inc     hl		;posuò se na promìnnou inverz./norm. reim
f16b 7e        ld      a,(hl)		;otestuj
f16c e601      and     01h		;reim
f16e 79        ld      a,c		;do A znak co se bude tisknout
f16f ca74f1    jp      z,0f174h		;pro normální reim odskoè dál
f172 f680      or      80h		;nastav bit 7 pro inv. reim
f174 2a0c00    ld      hl,(000ch)	;vyzvedni aktuální adresu ve videoram
f177 77        ld      (hl),a		;vlo na její místo danı znak
f178 210e00    ld      hl,000eh		;do HL adresa promìnné akt. sloupec
f17b 34        inc     (hl)		;zvy hodnotu akt. sloupce
f17c 3a1f00    ld      a,(001fh)	;vyzvedni poèet znakù na øádek
f17f 3d        dec     a		;sni o jedna (0-x)
f180 be        cp      (hl)		;jsi za posledním znaku øádku?
f181 f0        ret     p		;návrat pokud ne
f182 3600      ld      (hl),00h		;novı øádek tedy sloupec nastav 0
f184 23        inc     hl		;posuò se na aktuální øádek
f185 34        inc     (hl)		;zvyš hodnotu øádku
f186 3a1300    ld      a,(0013h)	;vyzvedni délku stránky
f189 be        cp      (hl)		;porovnej s akt. øádkem
f18a f0        ret     p		;a pokud netøeba rolovat návrat
f18b 35        dec     (hl)		;jinak sni akt. øádek
f18c cd95f9    call    0f995h		;roluj obrazovku nahoru 
f18f c9        ret     			;návrat

;Sem skáèe rutina F007 po vytištìní znaku
;----------------------------------------
f190 0600      ld      b,00h		;vynuluj B
f192 3a1f00    ld      a,(001fh)	;poèet znakù na øádku do A
f195 4f        ld      c,a		;a do C
f196 3a0f00    ld      a,(000fh)	;do A akt. øádek
f199 2a2000    ld      hl,(0020h)	;do HL adresa Videoram
f19c fe        db      0FEh		;fígl: neprovádìj "add" pøed prvním "dec A" (cp N)
f19d 09        add     hl,bc		;vypoèítej adresu øádku ve Videoram
f19e 3d        dec     a		;souètem aktuální-øádek x poèet znakù na øádku
f19f f29df1    jp      p,0f19dh		;opakuj pro všecky øádky
f1a2 3a0e00    ld      a,(000eh)	;vyzvedni akt. sloupec
f1a5 4f        ld      c,a		;dej do C (B=0)
f1a6 09        add     hl,bc		;a dopoèítej vıslednou adresu akt. znaku do HL
f1a7 220c00    ld      (000ch),hl	;ulo na patøiènou promìnnou
f1aa 7e        ld      a,(hl)		;vyzvedni aktuální znak na pozici kurzoru
f1ab 320b00    ld      (000bh),a	;a dej ho na patøiènou promìnnou
f1ae e1        pop     hl		;obnov uloené registry pøed tiskem a
f1af d1        pop     de
f1b0 c1        pop     bc
f1b1 f1        pop     af
f1b2 c9        ret     			;konec F007 (tisk znaku z C vèetnì øídících kódù)

;pokraèování startu - inicializace 8259
;--------------------------------------
f1b3 e1        pop     hl		;vyzvedni adresu zásobníku do HL 
f1b4 3ef7      ld      a,0f7h		;inicializace 8259, reim s nástupnou hranou
					;a potlaèením pøerušení stejné a niší úrovnì
					;1111 0111
					;111-adresa a7-a5  1-ICW1 0-hrana 1-po 4 bajtech
					;1-single  1-ICW4 ano
f1b6 d388      out     (88h),a		;pošli ICW1
f1b8 7c        ld      a,h		;nastav adresu tabulky rutin pøerušení (a8-a15)
f1b9 d389      out     (89h),a		;ICW2 - podle konce RAM - registr H (7F pro 32kb)
f1bb af        xor     a		;reim s manuálním ukonèováním pøerušení
					;0000 0000
					;000 0-ne specialní reim 00 - ne bufferovanı reim
					;0-manuální konec 0-8080(5) reim
f1bc d389      out     (89h),a		;pošli ICW4
f1be 3e9f      ld      a,9fh		;maska pøerušení 10011111 - povoluje pøerušení 5 a 6
d1c0 d389      out     (89h),a		;OCW1 - co jsou 50Hz a tlaèítko BR
f1c2 3e20      ld      a,20h		;povol pøerušení 8259 (pøíkaz konec pøerušení)
f1c4 d388      out     (88h),a		;OCW2 0010 0000
					;001 - nespecifikovanı konec pøerušení
f1c6 fb        ei      			;povol pøerušení procesoru
f1c7 3a00c8    ld      a,(0c800h)	;test jestli je pøítomen
f1ca 3c        inc     a		;modul Basic6, Basic G nebo jinı
f1cb c200c8    jp      nz,0c800h	;pokud ano pokraèuj tímto modulem na C800h
f1ce 2136f2    ld      hl,0f236h	;jinak je monitor a vypiš
f1d1 cd88f4    call    0f488h		;hlášení "Monitor >"

;hlavní smyèka monitoru
;----------------------
f1d4 2a0400    ld      hl,(0004h)	;obnov vrchol zásobníku podle konce RAM
f1d7 2ec2      ld      l,0c2h
f1d9 f9        ld      sp,hl		;nastav SP
f1da 3e9f      ld      a,9fh		;odmaskuj pøerušení 5 a 6
f1dc d389      out     (89h),a		;na 8259
f1de fb        ei      			;povol pøerušení
f1df cd47f6    call    0f647h		;tisk následujícího znaku
f1e2 3e        db      ">"
f1e3 cd00f0    call    0f000h  		;znak z klávesnice na obrazovku a do C
f1e6 79        ld      a,c		;kód klávesy do A pro porovnání
f1e7 2109f2    ld      hl,0f209h	;tabulka písmenek pøíkazù monitoru do HL
f1ea 010a00    ld      bc,000ah		;je 10 pøíkazù
f1ed be        cp      (hl)		;porovnat kód klávesy s tabulkou
f1ee caf9f1    jp      z,0f1f9h		;shoda, pokraèuj vıpoètem adresy pøíkazu
f1f1 23        inc     hl		;posun na další znak v tabulce
f1f2 0d        dec     c		;opakujeme 10x
f1f3 c2edf1    jp      nz,0f1edh	;do konce tabulky
f1f6 c327f2    jp      0f227h		;nenalezeno, vypiš ? a jdi zpìt na hlavní smyèku


;pokraèování hlavní smyèky pøi nalezení pøíkazu monitoru
;-------------------------------------------------------
f1f9 2111f2    ld      hl,0f211h	;adresa tabulky pøíkazù monitoru -2
f1fc 09        add     hl,bc		;poèítáme adresu podle poøadí písmenka v tabulce
f1fd 09        add     hl,bc
f1fe 01d4f1    ld      bc,0f1d4h	;návratová adresa (smyèka monitoru)
f201 c5        push    bc		;na zásobník
f202 0602      ld      b,02h		;vìtšina pøíkazù má 2 parametry (dle potøeby se mìní
					;v jednotlivıch rutinách)
f204 7e        ld      a,(hl)		;vyzvedni adresu
f205 23        inc     hl		;pøíkazu monitoru z tabulky
f206 66        ld      h,(hl)		;do registru HL
f207 6f        ld      l,a
f208 e9        jp      (hl)		;a skoè na ní

;tabulka písmenek pøíkazù monitoru
;---------------------------------
f209 52534347  db      "RSCG"		;jednopísmenové pøíkazy
f20d 4c445857  db      "LDXW" 
f212 4d46      db      "MF"

;tabulka odpovídajících adres pøíkazù monitoru
;---------------------------------------------
f213 51f2      dw      0f251h		;F - Fill - naplnìní èásti pamìti hodnotou
f215 40f2      dw      0f240h		;M - Move - pøesun èásti pamìti
f217 60f2      dw      0f260h		;W - Write - uloení na magnetofon
f219 c4f2      dw      0f2c4h		;X - Change - zmìna obsahu registrù/jejich vıpis
f21b 85f3      dw      0f385h		;D - Display - vypíše obsah èásti pamìti
f21d b6f3      dw      0f3b6h		;L - Load - z magnetofonu
f21f 05f4      dw      0f405h		;G - Goto - skok na adresu
f221 fcf3      dw      0f3fch		;C - Call - zavolání podprogramu na adrese
f223 38f4      dw      0f438h		;S - Subst - mìní obsah místa v pamìti
f225 84f4      dw      0f484h		;R - Return - návrat do Basicu (teplı start)


;neznámı pøíkaz monitoru
;-----------------------
f227 cd47f6    call    0f647h		;pošli na vıstupní zaøízení znak ?
f22a 3f        db      "?"		;protoe takovı pøíkaz monitor nezná
f22b cd47f6    call    0f647h		;pošli na vıstupní zaøízení znak odøádkuj
f22e 0a        db      0dh		;
f22f cd47f6    call    0f647h		;pošli na vıstupní zaøízení znak návrat vozíku
f232 0a        db      0ah	
f233 c3d4f1    jp      0f1d4h		;zpìt na smyèku monitoru (obnovit zásobník a èekat na
					;klávesu s pøíkazem monitoru)

;text hlášení monitoru
----------------------
f236 0d        db      0dh		;odøádkuj a nastav základní reim
f237 1f        db      01f		;sma obrazovku a kurzor na 0,0     
f238 4d4f4e    db      "MONITOR"
f23b 49544f52
f23f 8d        db      0dh+80h		;odøádkuj s nastavenım nejvyšším bitem jako konec textu

;pøíkaz M monitoru - Pøesun bloku pamìti
;má 3 parametry - od do kam
;---------------------------------------
f240 04        inc     b		;zvyš poèet parametrù
f241 cde5f4    call    0f4e5h		;vyzvedni parametry
f244 c1        pop     bc		;a dej je do odpovídajících registrù
f245 d1        pop     de
f246 e1        pop     hl

;rutina na pøenos bajtù z adresy od HL do DE na adresu danou BC
;--------------------------------------------------------------
f247 7e        ld      a,(hl)		;kopie bajtu z (HL)
f248 02        ld      (bc),a		;na (BC)
f249 03        inc     bc		;posun na další cílovou adresu
f24a cd9bf4    call    0f49bh		;zvyš HL a otestuj HL=0 nebo HL=DE
f24d d247f2    jp      nc,0f247h	;pokud ne kopíruj další bajt 
f250 c9        ret     			;hotovo

;pøíkaz F monitoru - Naplnìní èásti pamìti hodnotou
;má 3 parametry - od do èím
;--------------------------------------------------
f251 04        inc     b		;zvyš poèet parametrù
f252 cde5f4    call    0f4e5h		;vyzvedni parametry
f255 c1        pop     bc		;a dej je do odpovídajících registrù
f256 d1        pop     de
f257 e1        pop     hl
f258 71        ld      (hl),c		;dej na danou adersu poadovanı znak
f259 cd9bf4    call    0f49bh		;zvyš HL a otestuj HL=0 nebo HL=DE
f25c d258f2    jp      nc,0f258h	;opakuj dokud není vše vyplnìno
f25f c9        ret     			;návrat

;Pøíkaz W monitoru
;nahrání dat na magnetofon - parametry: od, do, start (0 pro nestart)
;pouívá F66B - 4.5. bit IO bajtu na rozlišení periferie (st. magnetofon)
;kontrolní souèet je souèet všech bajtù bloku se zanedbáním pøenosu pøes 256
;na konci invertovanı a pøiètena 1èka
;jeden blok vypadá takto:
;1. bajt ":" (1 bajt)
;2. poèet bajtù X v bloku (1 bajt)
;3. adresa kam se nahrává (2 bajty) (pro závìreènı blok je tady startovací adresa)
;4. oddìlovaè - 0 pro normální blok- 80 bajtù nebo poslední do konce nahrávané oblasti
;               1 pro závìreènı blok (1 bajt) (závìreènı blok nemá ádná data)
;5. jednotlivé datové bajty bloku (X krát 1 bajt)
;6. kontrolní souèet (1 bajt)
;7. 2 bajty 0Dh a 0Ah
;-------------------------------------------------------------------------------------
f260 04        inc     b		;3 parametry
f261 cde5f4    call    0f4e5h		;vyzvedni je
f264 c1        pop     bc		;a dej do odpovídajících registrù
f265 d1        pop     de
f266 e1        pop     hl
;nahrání dat na magnetofon od HL do DE, startovací adresa v BC
--------------------------------------------------------------
f267 c5        push    bc		;uschovej startovací adresu (0 pokud se nestartuje)
f268 cdfcf6    call    0f6fch		;start magnetofonu nebo dìrovaèe pøi nahrávání
f26b e5        push    hl		;uschovej adresu kam budem nahrávat blok
f26c 015000    ld      bc,0050h		;nahráváme bloky o 50h bajtech
f26f 09        add     hl,bc		;pøièti k adrese kam se bude nahrávat
f270 cda0f4    call    0f4a0h		;porovnej zda je vısledek vìtší ne koncová adresa
					;pokud ano je CY=1
f273 e1        pop     hl		;obnov adresu kam se bude nahrávat
f274 79        ld      a,c		;do A dáme 80 (50h) - poèet bajtù v bloku
f275 d27bf2    jp      nc,0f27bh	;pokud ještì nenahráváme poslední blok odskoè dál
f278 7b        ld      a,e		;jinak uprav poèet znakù v bloku tak 
f279 95        sub     l		;aby odpovídal koncové adrese
f27a 3c        inc     a
f27b d5        push    de		;uschovej koncovou adresu
f27c 47        ld      b,a		;poèítadlo bajtù do B
f27d 1600      ld      d,00h		;D se pouívá jako pomocné pro kontrolní souèet
					;take si ho tady pøed kadım blokem vynulujeme
f27f cd67f6    call    0f667h		;pošli na periferii následující znak (podle F66B)
f282 3a	       db      03ah		;bajt ":"
f283 78        ld      a,b		;poèet bajtù v bloku do A
f284 cde8f5    call    0f5e8		;pošli na periferii bajt z A (podle F66B)
f287 b3        or      e		;pokud je v A i E nula je konec
f288 caacf2    jp      z,0f2ach		;a zapiš závìreènı ukonèovací blok 
f28b cde3f5    call    0f5e3h		;adresa v HL na periferii
f28e af        xor     a		;odìlovaè 0 do A - není poslední blok (ten má 1-èku)
f28f cde8f5    call    0f5e8h		;a na periferii
;smyèka pro blok
f292 7e        ld      a,(hl)		;vyzvedni z adresy bajt;
f293 23        inc     hl		;zvyš adresu
f294 cde8f5    call    0f5e8h		;pošli ho na periferii
f297 05        dec     b		;sni poèítadlo
f298 c292f2    jp      nz,0f292h	;opakuj do konce bloku
f29b 2f        cpl     			;vypoèítáme hodnotu
f29c 3c        inc     a		;kontrolního souètu
f29d cde8f5    call    0f5e8h		;a vypiš na periferii
f2a0 cd67f6    call    0f667h		;následující bajt - 0Dh - na periferii
f2a3 0d        db      0dh
f2a4 cd67f6    call    0f667h		;následující bajt - 0Ah - na periferii
f2a7 0a        db      0ah
f2a8 d1        pop     de		;obnov uschovanou koncovou adresu
f2a9 c36bf2    jp      0f26bh		;zpátky na pøípravu dalšího bloku (buï celı nebo 
					;èást do konce poslední)

f2ac e1        pop     hl		;koncovou adresu u nepotøebujeme /pryè ze zásobníku
f2ad e1        pop     hl		;vyzvedneme uloenou startovací adresu (0 nestart)
f2ae cde3f5    call    0f5e3h		;a pošleme ji na periferii
f2b1 3e01      ld      a,01h		;závìreènı blok obsahuje pouze bajt s hodnotou 1
f2b3 cde8f5    call    0f5e8h		;a dej ho na periferii
f2b6 2f        cpl     			;vypoèti kotrolní
f2b7 3c        inc     a		;souèet
f2b8 cde8f5    call    0f5e8h		;a dej ho na periferii
f2bb cd67f6    call    0f667h		;následující bajt - 0Dh - na periferii
f2be 0d        db      0dh
f2bf cd67f6    call    0f667h		;následující bajt - 0Ah - na periferii
f2c2 0a        db      0ah		;(odøádkování a pøesun na zaèátek dalšího øádku)
f2c3 c9        ret     			;návrat - hotovo uloeno

;pøíkaz X monitoru (jak bez tak s parametrem)
;bez parametru pouze vypíše obsah registrù uloenıch na 0022-002D,
;jinak zmìna obsahu registru
;-----------------------------------------------------------------
f2c4 cd00f0    call    0f000h		;naèti a vytkni další znak z klávesnice
f2c7 79        ld      a,c		;dej ho do A pro test jestli
f2c8 fe0d      cp      0dh		;je to CR? (konec øádku)
f2ca c2edf2    jp      nz,0f2edh	;pokud ne odskoè dál na X s parametrem
					;jinak je pouze
;vıpis obsahu registrù z adres 0022-002D 
----------------------------------------
f2cd 2135f3    ld      hl,0f335h	;do HL tabulka s názvy registrù a odøádkování
f2d0 cd88f4    call    0f488h		;vytiskni tabulku
f2d3 f3        di			;zaka pøerušení     
f2d4 112200    ld      de,0022h		;0022 - adresa bufferu registrù
f2d7 0606      ld      b,06h		;tiskneme 6x postupnì obsah hodnot všech registrù
f2d9 eb        ex      de,hl		;které jsou na adresách 0022-002D
f2da 5e        ld      e,(hl)		;AF, BC, DE, HL, SP, PC 
f2db 23        inc     hl
f2dc 56        ld      d,(hl)
f2dd 23        inc     hl
f2de eb        ex      de,hl
f2df cdd0f5    call    0f5d0h		;vypiš HL hexadecimálnì (4 znaky)
f2e2 cd47f6    call    0f647h		;vytiskni mezeru
f2e5 20        db      " "
f2e6 05        dec     b		;u je všech 6?
f2e7 c2d9f2    jp      nz,0f2d9h	;pokud ne tiskni další
f2ea c32bf2    jp      0f22bh		;odøádkuj a skoè do hlavní smyèky monitoru

;X - modifikace obsahu registru
;pøíkazy XA, XF, XB, XC, XD, XE, XH, XL, XS (pro SP) a XP (pro PC) 
;jméno registru jako znak v A 
;----------------------------------------------------------------
f2ed 2166f3    ld      hl,0f366h	;tabulka registrù a jejich adres
f2f0 0e0a      ld      c,0ah		;máme 10 registrù které je mono mìnit
f2f2 be        cp      (hl)		;hledáme kterı se mìní
f2f3 ca00f3    jp      z,0f300h		;pokud je to on odskoè dál
f2f6 23        inc     hl		;posun v tabulce
f2f7 23        inc     hl
f2f8 23        inc     hl
f2f9 0d        dec     c		;projedem celou tabulku
f2fa c2f2f2    jp      nz,0f2f2h	;dokud nejsou vyzkoušeny všechny monosti
f2fd c327f2    jp      0f227h		;neznámı pøíkaz (špatné jméno registru)
					;vypíše otazník, odøádkuje a pak smyèka monitoru
;pokraèování X pøi nalezení kterı registr se bude mìnit
f300 cd47f6    call    0f647h		;vypiš " "
f303 20        db      " "
f304 cd54f3    call    0f354h		;vypiš obsah daného registru (SP PC jako celek)
					;a dej jeho adresu do DE a do B FF/0 podle poètu bajtù
f307 cd47f6    call    0f647h		;vypiš "-"
f30a 2d        db      "-"
f30b cd00f0    call    0f000h		;znak z klávesnice rovnou vytisknout (a do A)
f30e 79        ld      a,c
f30f cdb7f4    call    0f4b7h		;otestuj CR/SP
f312 d8        ret     c		;návrat pokud bylo CR
f313 ca29f3    jp      z,0f329h		;pokud je SP pokraèuj dál (posuò se na další registr)
f316 e5        push    hl		;uschovej HL
f317 c5        push    bc		;uschovej BC (v C znak)
f318 210000    ld      hl,0000h		;poèáteèní hodnota èísla na 0
f31b cdc9f4    call    0f4c9h		;naèti èíslo z C a další èíslice z klávesnice do HL
f31e 7d        ld      a,l		;vezmi hodnotu registru (0-FF)
f31f 12        ld      (de),a		;a dej ji na promìnnou daného registru
f320 f1        pop     af		;v B a teï v A je poèet bajtù (1/2>255/0)
f321 b7        or      a		;test
f322 fa28f3    jp      m,0f328h		;šlo-li o jednobajtovı registr poskoè
f325 13        inc     de		;jinak ulo
f326 7c        ld      a,h		;vyšší bajt na
f327 12        ld      (de),a		;promìnnou daného registru
f328 e1        pop     hl		;obnov HL
f329 af        xor     a		;do A dej 0
f32a b6        or      (hl)		;vyzvedni další hodnotu z tabulky registrù
f32b fab9f5    jp      m,0f5b9h		;pokud si na konci návrat pøes odøádkování
f32e 79        ld      a,c		;dej do A poslední znak z klávesnice
f32f fe0d      cp      0dh		;pokud to byl CR
f331 c8        ret     z		;tak návrat
f332 c304f3    jp      0f304h		;jinak opakuj pro další registr

;tabulka s názvy registrù a odøádkování
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

;X - vypiš obsah daného registru (SP PC jako celek)
;v HL je adresa s pozicí znaku registru v tabulce F366
;nastaví DE na adresu registru v promìnnıch monitoru
;a B podle délky (FF/0 pro 1/2 bajty)
;-----------------------------------------------------
f354 23        inc     hl		;posuò se na dolní bajt adresy
f355 5e        ld      e,(hl)		;adresu daného registru z bufferu registrù
f356 1600      ld      d,00h		;dej do DE
f358 23        inc     hl		;posuò se na další bajt, co je indikátor poètu bajtù 
f359 46        ld      b,(hl)		;které se budou tisknout (1 je pro SP, PC - 2 bajty)
f35a 23        inc     hl		;a posuò se na další registr
f35b 1a        ld      a,(de)		;vyzvedni hodnotu daného registru z bufferu
f35c cdd5f5    call    0f5d5h		;vytiskni ji (v HEX tvaru)
f35f 05        dec     b		;test jestli ještì tisknout / SP nebo PC
					;do B dej FF pro 1 bajt a 0 pro 2
f360 f8        ret     m		;pokud ne vra se
f361 1b        dec     de		;další bajt na tisknutí je o 1 ní v bufferu	
f362 1a        ld      a,(de)		;vyzvedni jeho hodnotu
f363 c3d5f5    jp      0f5d5h		;a vytiskni ji s návratem zpìt (obvykle na F307)


;tabulka registrù a jejich adres a poètu bajtù které tisknout
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

;pøíkaz monitoru D
;-----------------
f385 05        dec     b		;jen jeden parametr (adresa)
f386 cde5f4    call    0f4e5h		;naèti ho
f389 e1        pop     hl		;a dej do HL
;D od adresy v HL
-----------------
f38a 3a1300    ld      a,(0013h)	;vyzvedni délku stránky
f38d 0f        rrca    			;dìlíme 2
f38e 47        ld      b,a		;dej do B
f38f 05        dec     b		;a sni B o jedna
f390 caabf3    jp      z,0f3abh		;odskoè pryè pokud je obrazovka u zaplnìná
f393 cdb9f5    call    0f5b9h		;odøádkuj podle F6AB
f396 cdfaf5    call    0f5fah		;vypiš HL podle F6AB
f399 cda7f6    call    0f6a7h		;vytiskni následující bajt podle F6AB
f39c 207e      db      " "		;mezera
f39c 7e        ld      a,(hl)		;vyzvedni obsah z HL (danı parametr)
f39e cdfff5    call    0f5ffh		;vypiš A podle F6AB
f3a1 23        inc     hl		;zvyš adresu
f3a2 7d        ld      a,l		;niší bajt do a
f3a3 e607      and     07h		;nech pouze spodní 3 bity (0-7)
f3a5 c299f3    jp      nz,0f399h	;opakuj dokud jich není 8 na øádku
f3a8 c38ff3    jp      0f38fh		;opakuj do zaplnìní obrazovky na novém øádku

;u je zaplnìná celá obrazovka
;-----------------------------
f3ab cd2bf6    call    0f62bh		;vstup z periferie podle 2 niších bitù IO
f3ae fe0d      cp      0dh		;je to CR?
f3b0 cab9f5    jp      z,0f5b9h		;pokud ano návrat pøes odøádkování podle F6AB
f3b3 c38af3    jp      0f38ah		;jinak opakuj vıpis

;pøíkaz L monitoru- Load z magnetofonu/dìrné pásky
;=================================================
f3b6 05        dec     b		;pouze jeden parametr
f3b7 cde5f4    call    0f4e5h		;vyzvedni jej a dej na zásobník
f3ba dbf8      in      a,(0f8h)		;èti port A modulu Staper - èteèka dìrné pásky
f3bc cd11f7    call    0f711h		;start magnetofonu a pøíprava obrazovky na load
f3bf cd0bf5    call    0f50bh		;vstup bajtu do A z periferie podle F689
f3c2 d63a      sub     3ah		;odeèti 58 (":")
f3c4 c2bff3    jp      nz,0f3bfh	;opakuj dokud není dvojteèka
f3c7 57        ld      d,a		;kontrolní souèet nastavit na 0
f3c8 cdf4f4    call    0f4f4h		;2 èíslice v Ascii jako bajt do A podle F689
f3cb 5f        ld      e,a		;dej do E (poèet bajtù)
f3cc cdf4f4    call    0f4f4h		;2 èíslice v Ascii jako bajt do A podle F689
f3cf 67        ld      h,a		;do H (vyšší bajt adresy)
f3d0 cdf4f4    call    0f4f4h		;2 èíslice v Ascii jako bajt do A podle F689
f3d3 6f        ld      l,a		;do L (niší bajt adresy)
f3d4 7b        ld      a,e		;test poètu bajtù v bloku
f3d5 a7        and     a		;na nulu
f3d6 c2e4f3    jp      nz,0f3e4h	;pokud to není poslední blok (nulová délka) odskoè
f3d9 7d        ld      a,l		;v pøípadì posledníhi bloku je v HL adresa autostartu
f3da b4        or      h		;ale nestartovat pokud je 0
f3db d1        pop     de		;do DE dej parametr pøíkazu L
f3dc cad4f1    jp      z,0f1d4h		;pokud není autostart, skoè na hlavní smyèku monitoru
f3df 3e9f      ld      a,9fh		;jinak odmaskuj pøerušení
f3e1 d389      out     (89h),a
f3e3 e9        jp      (hl)		;a skoè na danou adresu

;ètení bloku dat z periferie (mgf/dìrné pásky)
;---------------------------------------------
f3e4 c1        pop     bc		;do BC parametr pøíkazu (nebo 0 pokud není)
f3e5 c5        push    bc		;a zpátky na zásobník pro pøíští pouití
f3e6 09        add     hl,bc		;pøièti k adrese daného bloku
f3e7 cdf4f4    call    0f4f4h		;2 èíslice v Ascii jako bajt do A podle F689
					;(oddìlovací bajt)
f3ea cdf4f4    call    0f4f4h		;2 èíslice v Ascii jako bajt do A podle F689
f3ed 77        ld      (hl),a		;ulo danı bajt na jeho místo
f3ee 23        inc     hl		;zvyš adresu
f3ef 1d        dec     e		;sni poèítadlo bajtù v bloku
f3f0 c2eaf3    jp      nz,0f3eah	;opakuj pro všechny bajty bloku
f3f3 cdf4f4    call    0f4f4h		;naèti kontrolní souèet (2 èíslice podle F689)
f3f6 cabff3    jp      z,0f3bfh		;a pokud je vše OK skoè na ètení dalšího bloku
f3f9 c327f2    jp      0f227h		;vypiš ? pøi chybì a zpìt do smyèky monitoru

;pøíkaz C monitoru
;=================
f3fc 2a2a00    ld      hl,(002ah)	;do HL obsah systémové promìnné SP na 02Ah
f3ff 1132f4    ld      de,0f432h	;dej na vrchol zásobníku
f402 73        ld      (hl),e		;návratovou adresu
f403 23        inc     hl		;0F432h
f404 72        ld      (hl),d
f405 cd00f0    call    0f000h		;znak z klávesnice na obrazovku (I/O=69)
f408 79        ld      a,c		;dej si znak do A
f409 cdb7f4    call    0f4b7h		;otestuj " ","," a CR
f40c da1ef4    jp      c,0f41eh		;pokud je pouze C odskoè dále (C a CR)
f40f ca27f2    jp      z,0f227h		;není povolena ani mezera ani èárka, odkoè na chybu
f412 210000    ld      hl,0000h		;vynuluj HL
f415 cdc9f4    call    0f4c9h		;vyzvedni adresu podprogramu kterı zavolat
f418 d227f2    jp      nc,0f227h	;pokud je špatná skok na chybu
f41b 222c00    ld      (002ch),hl	;ulo do systémové promìnné PC na 02Ch
f41e f3        di      			;zaka pøerušení
f41f 312200    ld      sp,0022h		;vyzvedni registry podle systémovıch promìnnıch
f422 f1        pop     af		;od adresy 022h
f423 c1        pop     bc
f424 d1        pop     de
f425 2a2a00    ld      hl,(002ah)	;vyzvedni SP
f428 f9        ld      sp,hl
f429 2a2c00    ld      hl,(002ch)	;vyzvedni adresu podprogramu kterı volat
f42c e5        push    hl		;dej ji na zádobník
f42d 2a2800    ld      hl,(0028h)	;vyzvedni HL
f430 fb        ei      			;povol pøerušení
f431 c9        ret     			;skok na podprogram

;návrat po C sem
;---------------
f432 cd0df6    call    0f60dh		;ulo registry do promìnnıch monitoru
f435 c3d4f1    jp      0f1d4h		;skok zpìt do hlavní smyèky monitoru

;pøíkaz S monitoru - zmìna obsahu pamìti
;pozor, neodesílá se CR ale SP !!!!
;=======================================
f438 cdc3f4    call    0f4c3h		;vstup èísla z klávesnice do HL
f43b d8        ret     c		;návrat pokud CR (S musí bıt zakonèené mezerou!)
f43c fe2c      cp      2ch		;je to ","?
f43e ca27f2    jp      z,0f227h		;vypiš ? pokud ano a zpìt do smyèky monitoru
f441 cdb0f5    call    0f5b0h		;odøádkuj podle F64B
f444 cdd0f5    call    0f5d0h		;vypiš HL hexadecimálnì podle F64B
f447 cd47f6    call    0f647h		;vypiš následující znak
f44a 20        db      " "		;mezera
f44b 7e        ld      a,(hl)		;vyzvedni znak z dané adresy
f44c cdd5f5    call    0f5d5h		;vypiš A hexadecimálnì podle F64B
f44f cd47f6    call    0f647h		;vypiš následující znak
f452 2d        db      "-"
f453 cd2bf6    call    0f62bh		;vstup z per. podle 2 nejniš. bitù IO (klávesnice)
f456 4f        ld      c,a		;uschovej znak do C
f457 79        ld      a,c		;vezmi znak z C
f458 cdb7f4    call    0f4b7h		;otestuj ho
f45b da2bf2    jp      c,0f22bh		;pokud to bylo CR návrat do smyèky monitoru
					;pøes odøádkování
f45e ca7ef4    jp      z,0f47eh		;odskoè pokud to bylo SP (pøeskoèení na další adresu)
f461 fe19      cp      19h		;odskoè, pokud to
f463 ca80f4    jp      z,0f480h		;byla šipka nahoru
f466 cd4bf6    call    0f64bh		;vypiš danı znak podle 2 mejniš. bitù IO
f469 e5        push    hl		;uschvej HL (adresu)
f46a 210000    ld      hl,0000h		;poèáteèní hodnota pro vstup èísla
f46d cdc9f4    call    0f4c9h		;vstup èísla ze znaku z C a dalších z klávesnice do HL
f470 7d        ld      a,l		;bereme poslední 2 èíslice (niší bajt HL)
f471 e1        pop     hl		;obnov adresu
f472 77        ld      (hl),a		;ulo tam danou hodnotu
f473 be        cp      (hl)		;zkontroluj jestli se tam zapsala
f474 c227f2    jp      nz,0f227h	;pokud ne (ROM/místo kde není RAM), vypiš ?
					;a zpìt do smyèky monitoru
f477 79        ld      a,c		;do A poslední stisknutá klávesa
f478 fe0d      cp      0dh		;je to CR?
f47a c8        ret     z		;návrat pokud ano
f47b c357f4    jp      0f457h		;jinak opakujeme (test klávesy na SP/šipka nahoru)

;zmáèknuto SP-pøeskoèení na další adresu
;---------------------------------------
f47e 23        inc     hl		;zvyš adresu
f47f fe        db      0FEh		;finta na pøeskoèení následujícího bajtu (CP N)
;zmáèknuta šipka nahoru
;----------------------
f480 2b        dec     hl		;sni adresu (pøeskoèeno pokud jdeme pøes SP)
f481 c341f4    jp      0f441h		;a skoè zpìt do pøíkazu S na zpracování další adresy


;pøíkaz R monitoru - návrat
;skoèí na adresu která je uloena na 01Dh
;(napø. teplı start Basic, nebo Amos)
;========================================
f484 2a1d00    ld      hl,(001dh)	;vyzvedni adresu do HL
f487 e9        jp      (hl)		;a skoè na ní


;vıpis textu z (HL) podle F64B
;poslední bajt má 7 bit nastaven na 1
;------------------------------------
f488 7e        ld      a,(hl)		;vyzvedni znak z HL
f489 b7        or      a		;otestuj nejvyšší bit
f48a 4f        ld      c,a		;dej znak do C
f48b fa95f4    jp      m,0f495h		;pokud je to poslední znak odskoè
f48e cd4bf6    call    0f64bh		;vytiskni znak podle  nejn. 2 bitù IO
f491 23        inc     hl		;zvyš adresu
f492 c388f4    jp      0f488h		;opakuj dokud nejsou všechny
;poslední znak
;-------------
f495 e67f      and     7fh		;vynuluj nejvyšší bit
f497 4f        ld      c,a		;dej do C
f498 c34bf6    jp      0f64bh		;a návrat pøes vıpis

;zvıšení HL o 1 a test na 0 (Z) plus test HL>DE (CY)
;---------------------------------------------------
f49b 23        inc     hl		;zvyš HL
f49c 7c        ld      a,h		;test na nulu
f49d b5        or      l
f49e 37        scf
f49f c8        ret     z		;návrat s nastavenımi CY a Z pokud je HL 0
					;jinak porovnej DE s HL
;porovnání DE a HL (Z pøi nule, CY pokud HL>DE)
;----------------------------------------------
f4a0 7b        ld      a,e		;odeèti niší bajty
f4a1 95        sub     l
f4a2 7a        ld      a,d		;a pak i vyšší i s CY pokud byl nastaven
f4a3 9c        sbc     a,h
f4a4 c9        ret     			;návrat

;ASCII znak z A na HEX èíslo (0-F) opìt v A
;pokud to není 0-F je nastaveno CY na 1
;==========================================
f4a5 d630      sub     30h		;odeèti 48 ("0")
f4a7 d8        ret     c		;návrat pokud to byl znak s menším kódem ne 48
f4a8 c6e9      add     a,0e9h		;pøièti 233
f4aa d8        ret     c		;návrat pokud je znak "G" a vyšší
f4ab c606      add     a,06h		;pøièti 6
f4ad f2b3f4    jp      p,0f4b3h		;odskoè pro A-F
f4b0 c607      add     a,07h		;je to 0-9 ?
f4b2 d8        ret     c		;návrat pokud ne
f4b3 c60a      add     a,0ah		;uprav hodnotu na 0-15
f4b5 b7        or      a		;vynuluj CY
f4b6 c9        ret     			;návrat

;otestování znaku na SP,CR a ","
;pokud je to jeden z nich je nastaven Z
;pro CR ještì navíc CY
;--------------------------------------
f4b7 fe20      cp      20h		;je to SP?
f4b9 c8        ret     z		;návrat se Z pokud ano
f4ba fe2c      cp      2ch		;je to ","?
f4bc c8        ret     z		;návrat se Z pokud ano
f4bd fe0d      cp      0dh		;je to CR?
f4bf 37        scf     			;nastav CY
f4c0 c8        ret     z		;návrat se Z a CY pokud ano
f4c1 3f        ccf     			;vynuluj CY
f4c2 c9        ret     			;návrat

;Vstup hexa èísla z klávesnice do HL
;-----------------------------------
f4c3 210000    ld      hl,0000h		;poèáteèní hodnota je 0
f4c6 cd00f0    call    0f000h		;znak z Klávesnice vytiskout a do C
					
;Vstup hexa èísla z C a další z klávesnice do HL
;berou se pouze poslední 4 znaky
;CY pokud je poslední CR
;-----------------------------------
f4c9 79        ld      a,c		;znak do A
f4ca cda5f4    call    0f4a5h		;ASCII znak z A na èíslo (0-F) opìt v A
f4cd dad9f4    jp      c,0f4d9h		;pokud je to není èíselnı znak (0-9,A-F) odskoè
f4d0 29        add     hl,hl		;rotuj HL o 4 bity doprava
f4d1 29        add     hl,hl
f4d2 29        add     hl,hl
f4d3 29        add     hl,hl
f4d4 b5        or      l		;vynuluj CY
f4d5 6f        ld      l,a		;pøidej èíslici do HL na poslední 4 bity
f4d6 c3c6f4    jp      0f4c6h		;opakuj pro další klávesu

;pøi neèíselné klávese
;---------------------
f4d9 79        ld      a,c		;vyzvedni znak do A
f4da cdb7f4    call    0f4b7h		;otestování znaku na SP,CR a ","
f4dd c8        ret     z		;vra se pokud to byl jeden z nich
f4de c327f2    jp      0f227h		;jinak ? a zpìt do smyèky monitoru

;vstup B parametrù na zásobník
;vstupní bod je F4E5h
;-----------------------------
f4e1 f1        pop     af		;obnov AF
f4e2 da27f2    jp      c,0f227h		;ohlaš chybu pokud byl CR ale ne všechny parametry
;vstup zde
;---------
f4e5 cdc3f4    call    0f4c3h		;vstup èísla do HL z klávesnice (CY pokud CR)
f4e8 e3        ex      (sp),hl		;parametr na zásobník
f4e9 e5        push    hl		;a návratová adresa zpátky taky na zásobník
f4ea f5        push    af		;uschovej AF (CY pokud byla chyba)
f4eb 05        dec     b		;sni poèet parametrù
f4ec c2e1f4    jp      nz,0f4e1h	;opakuj dokud se nepøevzaly všechny potøebné
f4ef f1        pop     af		;obnov AF
f4f0 d227f2    jp      nc,0f227h	;pokud nebyl na konci CR ohlaš chybu
f4f3 c9        ret     			;návrat

;vstup 2 èíslic v ASC kódu z periferie podle IO v F689
;a vytvoøení 1 bajtu v A (páska/mgf)
;-----------------------------------------------------
f4f4 cd0bf5    call    0f50bh		;vstup do A podle F689
f4f7 cda5f4    call    0f4a5h		;pøevod Ascii znaku na èíslo
f4fa 07        rlca    			;rotuj doleva 4x (první èíslice)
f4fb 07        rlca    
f4fc 07        rlca    
f4fd 07        rlca    
f4fe 4f        ld      c,a		;uschovej si první èást do C
f4ff cd0bf5    call    0f50bh		;vstup do A podle F689
f502 cda5f4    call    0f4a5h		;pøevod Ascii znaku na èíslo
f505 b1        or      c		;pøidej první èíslici
f506 4f        ld      c,a		;vısledek uschovej do C
f507 82        add     a,d		;pøièti k D (kontrolní souèet)
f508 57        ld      d,a		;ulo zpìt do D
f509 79        ld      a,c		;vısledek zpìt do A
f50a c9        ret     			;návrat

;vstup 1 bajtu z periferie pøes IO podle F689 do A
;-------------------------------------------------
f50b cd89f6    call    0f689h		;bajt do A
f50e e67f      and     7fh		;nech pouze 7 bitù
f510 c9        ret     			;návrat


;Vstup bloku znakù do Mgf. bufferu a vstupní bod
;pro 1 bajt do A z mgf. pøes buffer je F51Fh
;-----------------------------------------------
f511 77        ld      (hl),a		;ulo znak do bufferu
f512 2c        inc     l		;zvyš adresu v bufferu
f513 d60d      sub     0dh		;byl znak CR?
f515 c23ef5    jp      nz,0f53eh	;pokud ne skoè na ètení dalšího bajtu
f518 6f        ld      l,a		;do L nulu
f519 221900    ld      (0019h),hl	;ulo adresu bufferu na promìnnou
f51c c1        pop     bc		;obnov BC
f51d d1        pop     de		;obnov DE
f51e fee5      db      0FEh		;finta na pøeskoèení push (cp 0e5h) pokud jdeme tudy
					;do následující rutiny

;znak z mgf. do A (pøes buffer !!)
;---------------------------------
f51f e5        push    hl		;uschovej HL (ne pokud pøichází zhora)
f520 2a1900    ld      hl,(0019h)	;vyzvedni adresu bufferu do HL
f523 2c        inc     l		;posuò se na následující znak
f524 221900    ld      (0019h),hl	;ulo adresu v bufferu zpìt
f527 ca38f5    jp      z,0f538h		;pokud byl buffer prázdnı skoè na naètení bloku dat
f52a 2d        dec     l		;sni zpìt adresu
f52b 7e        ld      a,(hl)		;vyzvedni znak do A
f52c fe0d      cp      0dh		;je to CR?
f52e c236f5    jp      nz,0f536h	;odskoè pokud ne
f531 2eff      ld      l,0ffh		;signál prázdnı buffer
f533 221900    ld      (0019h),hl	;ulo na promìnnou
f536 e1        pop     hl		;obnov HL
f537 c9        ret     			;návrat

;byl prázdnı buffer mgf, take budem naèítat blok bajtù dokud nebude CR
;----------------------------------------------------------------------
f538 d5        push    de		;uschovej DE
f539 3a1b00    ld      a,(001bh)	;vyzvedni promìnnou kazeáku do A (polarita, prodleva)
f53c 57        ld      d,a		;dej do D
f53d c5        push    bc		;uschovej i BC na zásobník
f53e db86      in      a,(86h)		;smyèka pro nalezení hrany
f540 aa        xor     d		;se správnou polaritou
f541 f23ef5    jp      p,0f53eh		;ze vstupu magnetofonu (opakuj dokud není)
f544 06ff      ld      b,0ffh		;do B poèáteèní hodnota FF (1111 1111b)
f546 1e09      ld      e,09h		;èteme 9 bitù (start + 8 bitù)
f548 db86      in      a,(86h)		;smyèka pro nalezení
f54a aa        xor     d		;opaèné hrany
f54b a8        xor     b		;start bitu
f54c f248f5    jp      p,0f548h		;signálu z mgf. (opakuj dokud není)
f54f 78        ld      a,b		;vyzvedni aktuální hodnotu tvoøeného bajtu
f550 1f        rra     			;pøirotuj z CY danı bit
f551 1d        dec     e		;sni poèítadlo bitù
f552 ca11f5    jp      z,0f511h		;pokud hotovo (všech 9) odskoè na uloení a test
f555 47        ld      b,a		;jinak ulo do B akt. hodnotu bajtu
f556 7a        ld      a,d		;vyzvedni si paremetr kazeáku (prodleva odp. periodì)
f557 e67f      and     7fh		;nech jen odpovídající bity (polarita nás nezajímá teï)
f559 3d        dec     a		;èekací smyèka podle A
f55a c259f5    jp      nz,0f559h	;vytvoøí poadovanou prodlevu
f55d db86      in      a,(86h)		;naèti hodnotu ze vstupu magnetofonu
f55f aa        xor     d		;uprav podle polarity
f560 e680      and     80h		;nech jen danı bit
f562 b0        or      b		;pøidej ho do B k atuální hodnotì bajtu
f563 47        ld      b,a		;ulo zpìt do B
f564 3c        inc     a		;v pøípadì e je v A FF
f565 ca46f5    jp      z,0f546h		;došlo k chybì a jedem bajt znovu (nebyl start bit
					;nebo stop bit)
f568 c348f5    jp      0f548h		;jinak další bit dokud nejsou všecky

;vıstup bajtu z C na magnetofon
;------------------------------
f56b 79        ld      a,c		;znak do A
f56c c5        push    bc		;uschovej BC
f56d f680      or      80h		;nastav nejvyšší bit na 1 (jako stop bit)
f56f 2f        cpl     			;nahraï 1 nulama a naopak
f570 4f        ld      c,a		;uschováme do C
f571 f3        di      			;zaka pøerušení, je to èasovì kritická operace
f572 cd93f5    call    0f593h		;nová perioda 1kHz
f575 3e01      ld      a,01h		;startovací bit
f577 d387      out     (87h),a		;na bit 0 portu 86 - pomocí bitového pøístupu pøes 87
f579 0608      ld      b,08h		;8 bitù (poèítadlo v B)
;smyèka
;------
f57b cd93f5    call    0f593h		;poèkáme 1 periodu
f57e 79        ld      a,c		;do A si vezmeme aktuální hodnotu vysílaného bajtu 
f57f e601      and     01h		;a necháme pouze 0-tı bit
f581 d387      out     (87h),a		;kterı vyšleme na port 86,0-tı bit (pøes port 87)
f583 79        ld      a,c		;posuneme se na další bit
f584 0f        rrca    			;pomocí rotace doprava
f585 4f        ld      c,a		;a uschováme aktuální hodnotu do C
f586 05        dec     b		;sníení poèítadla
f587 c27bf5    jp      nz,0f57bh	;a opakování smyèky dpkod není všech 8
f58a fb        ei      			;povolení pøerušení
f58b c1        pop     bc		;obnova uloeného BC
f58c 79        ld      a,c		;a pokud byl vysílanı bajt
f58d fe0d      cp      0dh		;kód 0Dh (CR)
f58f cca2f5    call    z,0f5a2h		;tak vytvoø meziblokovou pauzu (dle promìnné 001C)
f592 c9        ret     			;návrat

;poèkej 1 periodu signálu 1kHz z portu 86/bit5
;---------------------------------------------
f593 db86      in      a,(86h)		;naèti hodnotu
f595 e620      and     20h		;nech pøíslušnı bit
f597 ca93f5    jp      z,0f593h		;dokud je nulovı opakuj (1/2 periody)
f59a db86      in      a,(86h)		;a toté pro 2 pùl-periodu
f59c e620      and     20h
f59e c29af5    jp      nz,0f59ah	;jen odskok pro 1-èkovou hodnotu
f5a1 c9        ret     			;návrat

;mezibloková pauza po dobu (001C)x20 ms
;(hodnota z adresy 001C)
;--------------------------------------
f5a2 3a1c00    ld      a,(001ch)	;vyzvedni do A pøíslušnou hodnotu

;pauza po dobu Nx20 ms
;N je èíslo z A
;---------------------
f5a5 e5        push    hl		;uschovej HL
f5a6 210800    ld      hl,0008h		;do HL 0008 (nejniší bajt èítaèe pøerušení)
f5a9 86        add     a,(hl)		;vezmi souèasnou hodnotu èítaèe a pøièti poèet
					;kterı budeme èekat
;èekací smyèka
f5aa be        cp      (hl)		;rovná se nebo je vyšší hodnota èítaèe té potøebné?
f5ab f2aaf5    jp      p,0f5aah		;pokud ne èekáme
f5ae e1        pop     hl		;obnov HL
f5af c9        ret     			;návrat

;Odøádkování na periferii urèené I/O bajtem v rutinì F64B
;--------------------------------------------------------
f5b0 cd47f6    call    0f647h		;vypiš následující znak podle F64Bh
f5b3 0d        db      0Dh		;CR
f5b4 cd47f6    call    0f647h		;vypiš následující znak podle F64Bh
f5b7 0a        db      0Ah		;LF
f5b8 c9        ret     

;Odøádkování na periferii urèené I/O bajtem v rutinì F6AB
;--------------------------------------------------------
f5b9 cda7f6    call    0f6a7h		;vypiš následující znak podle F6ABh
f5bc 0d        db      0Dh		;CR
f5bd cda7f6    call    0f6a7h		;vypiš následující znak podle F6ABh
f5c0 0a        db      0Ah		;LF
f5c1 c9        ret     			;návrat


;pøevod horních 4 bitù registru A (0-F) 
;na 30h-39h (kód èíslice 0-9) nebo 41h-46h (kód znaku "A"-"F")
;-------------------------------------------------------------
f5c2 0f        rrca    			;posuò horní 4 bity na dolní
f5c3 0f        rrca    			
f5c4 0f        rrca    
f5c5 0f        rrca    			;a pokraèuj rutinou

;pøevod dolních 4 bitù registru A (0-F) 
;na 30h-39h (kód znaku "0"-"9") nebo 41h-46h (kód znaku "A"-"F")
;-------------------------------------------------------------
f5c6 e60f      and     0fh		;a vynuluj horní
f5c8 c690      add     a,90h		;9-ka do horních 4 bitù
f5ca 27        daa     			;pokud je ve spodních 4 bitech A-F, pøiète se 6
					;a bude tam 0-5 a nastaven Half-Carry a 9 se zmìní na A
f5cb ce40      adc     a,40h		;zvıšíme horní 4 bity o 4 (buï 9>D nebo A>E) 
					;a pokud byla zmìna v pøedchozí instrukci (A-F>0-5)
					;pøièteme jednièku (dostaneme 1-6 na dolních 4 bitech)
f5cd 27        daa     			;pøiète 60h - upraví horní 4 bity na 3(z D) nebo 4(z E)
					;a máme vısledek 30-39 nebo 41-46
f5ce 4f        ld      c,a		;vısledek do C
f5cf c9        ret     			;návrat

;Vıpis HL hexadecimálnì (4 èíslice) na periferii podle F64B
;----------------------------------------------------------
f5d0 7c        ld      a,h		;nejdøíve H
f5d1 cdd5f5    call    0f5d5h		;pøeveï a vytiskni
f5d4 7d        ld      a,l		;pak L

;Vıpis A hexadecimálnì (2 èíslice) na periferii podle F64B
;----------------------------------------------------------
f5d5 f5        push    af		;uschovej A
f5d6 cdc2f5    call    0f5c2h		;pøeveï horní 4 bity na znak
f5d9 cd4bf6    call    0f64bh		;a vytiskni ho na danou periferii
f5dc f1        pop     af		;obnov A
f5dd cdc6f5    call    0f5c6h		;pøeveï dolní 4 bity na znak
f5e0 c34bf6    jp      0f64bh		;vytiskni znak a návrat


;obsah HL na periferii podle F66B
;--------------------------------
f5e3 7c        ld      a,h		;nejdøív H
f5e4 cde8f5    call    0f5e8h		;a pošli na periferii
f5e7 7d        ld      a,l		;a pak L a návrat pøes
;znak z A na periferii podle F66B
;--------------------------------
f5e8 5f        ld      e,a		;uschovej si znak do E
f5e9 cdc2f5    call    0f5c2h		;horní 4 bity na písmeno 0-9 nebo A-F
f5ec cd6bf6    call    0f66bh		;na periferii
f5ef 7b        ld      a,e		;obnov znak z E
f5f0 cdc6f5    call    0f5c6h		;dolní 4 bity na písmeno 0-9 nebo A-F
f5f3 cd6bf6    call    0f66bh		;na periferii
f5f6 7b        ld      a,e		;obnov znak z E	
f5f7 82        add     a,d		;pøièti k D kde se poèítá kontr. souèet
f5f8 57        ld      d,a		;ulo zpìt do D
f5f9 c9        ret     			;návrat

;obsah HL na periferii podle F6AB
;--------------------------------
f5fa 7c        ld      a,h		;nejdøív H
f5fb cdfff5    call    0f5ffh		;a pošli na periferii
f5fe 7d        ld      a,l		;a pak L a návrat pøes

;znak z A na periferii podle F6AB
;--------------------------------
f5ff f5        push    af		;uschovej si znak na zásobník
f600 cdc2f5    call    0f5c2h		;horní 4 bity na písmeno 0-9 nebo A-F
f603 cdabf6    call    0f6abh		;na periferii
f606 f1        pop     af		;obnov znak
f607 cdc6f5    call    0f5c6h		;dolní 4 bity na písmeno 0-9 nebo A-F
f60a c3abf6    jp      0f6abh		;na periferii a návrat

;uloení registrù na promìnné 22-2B
;SP se nastaví na 7FC2 (32kB)
;----------------------------------
f60d 222800    ld      (0028h),hl	;ulo HL na 0028h
f610 210200    ld      hl,0002h		;SP se zvìtší o 2
f613 f5        push    af		;AF na zásobník (posune SP o 2)
f614 39        add     hl,sp		;SP+2 do HL
f615 f1        pop     af		;obnov AF
f616 222a00    ld      (002ah),hl	;ulo SP na 002Ah
f619 e1        pop     hl		;návratová adresa do HL
f61a f3        di			;zaka pøerušení      
f61b 312800    ld      sp,0028h		;nastav adresu pod kterou se uloí ostatní registry
f61e d5        push    de		;na 0026h DE
f61f c5        push    bc		;na 0024h BC
f620 f5        push    af		;na 0022h AF
f621 eb        ex      de,hl		;uschovej si návatovou adresu do DE
f622 2a0400    ld      hl,(0004h)	;konec pamìti do HL
f625 2ec2      ld      l,0c2h		;7FC2 pro 32kB
f627 f9        ld      sp,hl		;nastav SP
f628 fb        ei      			;povol pøerušení
f629 eb        ex      de,hl		;vyzvedni si návratvou adresu
f62a e9        jp      (hl)		;a vra se


;vstup bajtu z periferie podle nejniších dvou bitù do A
;-------------------------------------------------------
f62b 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f62e e603      and     03h		;nech pouze dolní 2 bity
f630 3ec2      ld      a,0c2h		;nastav A pro adresu 7FC2
f632 caf5f6    jp      z,0f6f5h		;a skoè na ni pokud jsou oba bity 0
f635 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f638 e603      and     03h		;nech pouze dolní 2 bity
f63a 3d        dec     a		;pokud byl bit0 na 1
f63b caaaf8    jp      z,0f8aah		;tak vstup znaku z klávesnice s èekáním a pípnutím
f63e 3d        dec     a		;pokud to byl bit 1 na 1
f63f caebf6    jp      z,0f6ebh		;tak vystup z dìrné pásky
f642 3ec5      ld      a,0c5h		;jinak nastav A pro adresu 7FC5
f644 c3f5f6    jp      0f6f5h		;a skoè na ni (oba bity na 1)

;vypiš znak za CALL na periferii podle F64B
;------------------------------------------
f647 e3        ex      (sp),hl		;vyzvedni adresu znaku a uschovej HL
f648 4e        ld      c,(hl)		;znak do C
f649 23        inc     hl		;zvyš adresu
f64a e3        ex      (sp),hl		;obnov HL a ulo návratovou adresu
					;a pokraèuj pøes F64Bh

;F64Bh vıstup na periferii podle 2 nejniších bitù IO
;-----------------------------------------------------
f64b 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f64e e603      and     03h		;nech pouze dolní 2 bity
f650 3ec8      ld      a,0c8h		;nastav A pro adresu 7FC8
f652 caf5f6    jp      z,0f6f5h		;a skoè na ni pokud jsou oba bity 0
f655 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f658 e603      and     03h		;nech pouze dolní 2 bity
f65a 3d        dec     a		;pokud byl pouze bit 0 na 1
f65b ca07f0    jp      z,0f007h		;skoè  na F007h (obrazovka)
f65e 3d        dec     a		;pokud to byl bit 1 na 1
f65f caabf6    jp      z,0f6abh		;skoè na F6AB (tisk podle bitù 6 a 7)
f662 3ecb      ld      a,0cbh		;jinak nastav A pro adresu 7FCBh
f664 c3f5f6    jp      0f6f5h		;a skoè na ni (oba bity na 1)

;vypiš znak za CALL na periferii podle F66B
;------------------------------------------
f667 e3        ex      (sp),hl		;vyzvedni adresu znaku a uschovej HL
f668 4e        ld      c,(hl)		;znak do C
f669 23        inc     hl		;zvyš adresu
f66a e3        ex      (sp),hl		;obnov HL a ulo návratovou adresu
					;a pokraèuj pøes F66Bh

;F66Bh vıstup na periferii podle bitù 4 a 5 IO
;---------------------------------------------
f66b 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f66e e630      and     30h		;nech pouze bity 4 a 5
f670 3ece      ld      a,0ceh		;nastav A pro adresu 7FCEh
f672 caf5f6    jp      z,0f6f5h		;a skoè na ni pokud jsou oba bity 0
f675 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f678 e630      and     30h		;nech pouze bity 4 a 5
f67a fe10      cp      10h		;je to bit 4?
f67c cad9f6    jp      z,0f6d9h		;vıstup na dìrovaè
f67f fe20      cp      20h		;je to bit 5?
f681 ca6bf5    jp      z,0f56bh		;vıstup na magnetofon
f684 3ed1      ld      a,0d1h		;jinak nastav A pro adresu 7FD1h
f686 c3f5f6    jp      0f6f5h		;a skoè na ni (oba bity na 1)

;vstup bajtu z periferie podle 2 a 3 bitu do A
;---------------------------------------------
f689 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f68c e60c      and     0ch		;nech pouze 2 a 3 bit
f68e 3ed4      ld      a,0d4h		;nastav A pro adresu 7FD4h
f690 caf5f6    jp      z,0f6f5h		;a skoè na ni pokud jsou oba bity nulové
f693 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f696 e60c      and     0ch		;nech pouze 2 a 3 bit
f698 fe04      cp      04h		;pokud je 2 bit na 1
f69a caebf6    jp      z,0f6ebh		;pak znak z dìrné pásky do A
f69d fe08      cp      08h		;pokud je 3 bit na 1
f69f ca1ff5    jp      z,0f51fh		;znak z mgf. do A (pøes buffer !!)
f6a2 3ed7      ld      a,0d7h		;jinak nastav A pro adresu 7FD7h
f6a4 c3f5f6    jp      0f6f5h		;a skoè na ni (oba bity na 1)

;vypiš znak za CALL na periferii podle F6AB
;------------------------------------------
f6a7 e3        ex      (sp),hl		;vyzvedni adresu znaku a uschovej HL
f6a8 4e        ld      c,(hl)		;znak do C
f6a9 23        inc     hl		;zvyš adresu
f6aa e3        ex      (sp),hl		;obnov HL a ulo návratovou adresu
					;a pokraèuj pøes F6ABh

;F6ABh vıstup na periferii podle bitù 6 a 7 IO
;---------------------------------------------
f6ab 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f6ae e6c0      and     0c0h		;nech pouze 6 a 7 bit
f6b0 3eda      ld      a,0dah		;nastav A pro adresu 7FDAh
f6b2 caf5f6    jp      z,0f6f5h		;a skoè na ni pokud jsou oba bity nulové
f6b5 3a0300    ld      a,(0003h)	;vyzvedni IO bajt
f6b8 e6c0      and     0c0h		;nech pouze 6 a 7 bit
f6ba fe40      cp      40h		;pokud byl bit 6 na 1
f6bc ca4bf6    jp      z,0f64bh		;tak vısup podle 2 nejniších 2 bitù IO
f6bf fe80      cp      80h		;byl pouze bit 7 na 1?
f6c1 3edd      ld      a,0ddh		;nastav A pro adresu 7FDDh	
f6c3 c2f5f6    jp      nz,0f6f5h	;a skoè na ni pokus ne (oba bity na 1)
					;jinak pokraèuj vıstupem na tiskárnu

;Vıstup znaku z C na tiskárnu
;----------------------------
f6c6 3e0c      ld      a,0ch
f6c8 d3fb      out     (0fbh),a
f6ca 3e0f      ld      a,0fh
f6cc d3fb      out     (0fbh),a
f6ce dbfa      in      a,(0fah)		;nyní poèkej a bude tiskárna
f6d0 e601      and     01h		;pøipravena pøijmout data
f6d2 cacef6    jp      z,0f6ceh		;pro tisk (opakuj dokud není)
f6d5 79        ld      a,c		;znak z C do A
f6d6 d3f9      out     (0f9h),a		;a na danı datovı port modulu STAPER (tiskárna)
f6d8 c9        ret     

;Vıstup znaku z C na dìrovaè
;---------------------------
f6d9 3e0e      ld      a,0eh
f6db d3fb      out     (0fbh),a
f6dd 3d        dec     a
f6de d3fb      out     (0fbh),a
f6e0 dbfa      in      a,(0fah)		;nyní poèkej a bude dìrovaè
f6e2 e601      and     01h		;pøipraven pøijmout data
f6e4 cae0f6    jp      z,0f6e0h		;pro vıstup (opakuj dokud není)
f6e7 79        ld      a,c		;znak z C do A
f6e8 d3f9      out     (0f9h),a		;a na danı datovı port modulu STAPER (dìrovaè)
f6ea c9        ret     			;návrat

;Vstup z dìrné pásky do registru A
;---------------------------------
f6eb dbfa      in      a,(0fah)		;dokud nejsou data
f6ed e608      and     08h		;pøipravena
f6ef caebf6    jp      z,0f6ebh		;opakuj test portu
f6f2 dbf8      in      a,(0f8h)		;pøeèti do A data z dìrné pásky
f6f4 c9        ret     			;návrat

;skok na adresu 7FXX kde XX je v A
;7F platí pro 32kB IQ
;---------------------------------
f6f5 e5        push    hl		;uschovej HL na zásobník
f6f6 2a0400    ld      hl,(0004h)	;vyzvedni konec RAM (7F do H)
f6f9 6f        ld      l,a		;niší bajt adresy z A do L
f6fa e3        ex      (sp),hl		;obnov HL a dej adresu na zásobník
f6fb c9        ret     			;skoè na ni

;start magnetofonu nebo dìrovaèe pøi nahrávání
;---------------------------------------------
f6fc 0603      ld      b,03h		;bit1 portu C-8255 (86) bude na 1 (0xxx001 1)
f6fe 3a0300    ld      a,(0003h)	;vyzvedni IO bajt do A
f701 e630      and     30h		;nech jen pøíslušné bity
f703 fe10      cp      10h		;je to dìrovaè?
f705 4f        ld      c,a		;uschovej A do C
f706 cc3bf7    call    z,0f73bh		;pokud ano skoè na rutinu dìrovaèe - vydìrování 64x0 
f709 79        ld      a,c		;obnov si IO bajt
f70a fe20      cp      20h		;pokud to není magnetofon
f70c c0        ret     nz		;tak se vra (oba bity IO na 1)
f70d 78        ld      a,b		;vyzvedni si hodnotu pro port 86
f70e d387      out     (87h),a		;a nastav pøíslušnı bit (bit1 na 1)
f710 c9        ret     			;návrat

;start mgf. a pøíprava obrazovky pøi vstupu dat z mgf. do poèítaèe
;-----------------------------------------------------------------
f711 0603      ld      b,03h		;hodnota pro zapnutí mgf. pøes port 87
f713 3a0300    ld      a,(0003h)	;IO bajt do A
f716 e60c      and     0ch		;nech pouze bity 2 a 3
f718 fe08      cp      08h		;pokud nebyl bit 3 na 1
f71a c0        ret     nz		;tak se vra
f71b 3edf      ld      a,0dfh		;vymaskuj pøerušení
f71d d389      out     (89h),a		;na øadièi 8259 (zákaz pøerušení)
f71f 3a0f00    ld      a,(000fh)	;vyzvedni èíslo øádku kde je kurzor
f722 d608      sub     08h		;sni o 8
f724 d230f7    jp      nc,0f730h	;odkoè pokud si byl na øádku 8 a více
f727 0e1a      ld      c,1ah		;jinak vypiš znak
f729 cd07f0    call    0f007h		;kurzor dolù (posune se dolù øádek)
f72c 3c        inc     a		;a opakuj
f72d c229f7    jp      nz,0f729h	;dokud nebude na øádku 8
f730 2a2000    ld      hl,(0020h)	;vyzvedni adresu Videoram
f733 2d        dec     l		;sni o jedna
f734 221900    ld      (0019h),hl	;a nastav tuto adresu jako buffer mgf.
f737 78        ld      a,b		;vyzvedni hodmotu na zapnutí mgf
f738 d387      out     (87h),a		;a pošli ji na danı port (bit 1 na 1 port C)
f73a c9        ret     			;návrat

;vıstup 64 nul na dìrovaè
;------------------------
f73b c5        push    bc		;uschovej BC na zásobník
f73c 010040    ld      bc,4000h		;64 v B jako poèítadlo, 0 do C
f73f cdd9f6    call    0f6d9h		;vıstup z C na dìrovaè
f742 05        dec     b		;sni poèítadlo
f743 c23ff7    jp      nz,0f73fh	;opakuj dokud si nevyslal všechny znaky
f746 c1        pop     bc		;obnov BC
f747 c9        ret     			;návrat

;obsluha pøerušení 50 Hz
;-----------------------
f748 f5        push    af		;uschovej registry
f749 e5        push    hl
f74a 3e20      ld      a,20h		;povol další pøerušení 8259
f74c d388      out     (88h),a
f74e fb        ei      			;povol pøerušení procesoru
f74f 210600    ld      hl,0006h		;do HL adresa èasování klávesnice a blikání kurzoru
f752 7e        ld      a,(hl)		;a její obsah do A
f753 a7        and     a		;test na 0
f754 c46cf7    call    nz,0f76ch	;pokud není nula nastav adresu 0006
f757 23        inc     hl		;v HL je 0007 - pøíznak blikání kurzoru
f758 7e        ld      a,(hl)		;dej pøíznak do A
f759 23        inc     hl		;posuò HL na 3-bajtovı èítaè 20 ms
f75a a7        and     a		;pokud A=0
f75b cc84f7    call    z,0f784h		;proveï blikání kurzorem
f75e 34        inc     (hl)		;následuje zvıšení 3-bajtového èítaèe 20ms
f75f c269f7    jp      nz,0f769h	;na adresách 0008-000A
f762 23        inc     hl
f763 34        inc     (hl)
f764 c269f7    jp      nz,0f769h
f767 23        inc     hl
f768 34        inc     (hl)
f769 e1        pop     hl		;obnov registry
f76a f1        pop     af
f76b c9        ret     			;a vra se zpìt z pøerušení

;nastavení adresy 0006 
;dìlá se v pøerušení 50Hz pokud je 006 nenulová
-----------------------------------------------
f76c db85      in      a,(85h)		;test je-li stisknuta
f76e 3c        inc     a		;nìjaká klávesa
f76f 7e        ld      a,(hl)		;obsah adresy 0006 do A (èasování klávesnice)
f770 ca7cf7    jp      z,0f77ch		;odskoè pokud není nic stisknuto
f773 3c        inc     a		;zvyš A
f774 e63f      and     3fh		;ponech dolních 6 bitù
f776 77        ld      (hl),a		;ulo zpátky na 0006
f777 fe21      cp      21h		;je toto èíslo menší ne 21h (33) ?
f779 f8        ret     m		;ano, vra se
f77a 35        dec     (hl)		;ne, sni o 1 a
f77b c9        ret     			;návrat
;kdy není klávesa, v A je obsah 0006
f77c a7        and     a		;nastav pøíznaky podle A
f77d 3600      ld      (hl),00h		;vynuluj èasovaè klávesnice
f77f f8        ret     m		;byl nejvyšší bit 1? pokud ano návrat
f780 c680      add     a,80h		;nastav nejvyšší bit na 1 v pùvodním obsahu 0006
f782 77        ld      (hl),a		;ulo na 0006 
f783 c9        ret     			;návrat

;blikání kurzorem pomocí pøerušení 50Hz
;kadé 4 pøerušení invertuje znak na pozici kurzoru
;--------------------------------------------------
f784 7e        ld      a,(hl)		;do A obsah 0008 (èítaè 20ms nejniší bajt)
f785 e603      and     03h		;ponech poslední 2 bity
f787 c0        ret     nz		;neblikej pokud nejsou 0
f788 e5        push    hl		;uschovej HL
f789 2a0c00    ld      hl,(000ch)	;do HL adresa kurzoru
f78c 7e        ld      a,(hl)		;do A znak kurzoru
f78d ee80      xor     80h		;invertuj nejvyšší bit (invertuj zobrazení znaku)
f78f 77        ld      (hl),a		;a ulo zpìt do Videoram
f790 e1        pop     hl		;obnov HL
f791 c9        ret     			;a vra se


;obsluha pøerušení od klávesy BR
;-------------------------------
f792 222800    ld      (0028h),hl	;uschovej si HL
f795 e1        pop     hl		;nastav adresu na kterou se bude 
f796 221500    ld      (0015h),hl	;po pøerušení vracet na pøíslušnou promìnnou (0015h)
f799 210400    ld      hl,0004h		;do HL dej adresu kde je promìnná konec RAM (0004)
f79c cd13f6    call    0f613h		;uschovej všechny ostatní registry
					;a SP nastav pod konec RAM
f79f cd47f6    call    0f647h		;vypiš následující znak - "#"
f7a2 23        db      "#"
f7a3 2a1500    ld      hl,(0015h)	;vypiš adresu na kterou se bude vracet, t.j.
f7a6 cdd0f5    call    0f5d0h		;kde byl program pøerušen (v hexa tvaru)
f7a9 3e20      ld      a,20h		;povol další pøerušení 8259
f7ab d388      out     (88h),a
f7ad fb        ei      			;a taky procesoru (jinak by nejela klávesnice)
f7ae c32bf2    jp      0f22bh		;odøádkuj a skoè do hlavní smyèky monitoru


;tabulka hodnot pro inicializaci monitoru
;----------------------------------------
f7b1>12 20     db      020h		;poèet posunovanıch znakù v pøíkazech DC a IL
f7b2>13 1e     db      01Eh		;délka stránky
f7b3>14 02     db      02h		;øádkování
f7b4>15 0000   dw      0000h		;návratová adresa monitoru pro pøíkaz R
f7b6>17 04     db      04h		;délka tónu
f7b7>18 12     db      012h		;vıška tónu
f7b8>19 00ec   dw      0EC00h		;adresa bufferu pro magnetofon
f7ba>1b 56     db      056h		;nastavení mgf. (polarita+prodleva)
f7bb>1c 21     db      021h		;mezibloková mezera na pásce
f7bc>1d 2bf2   dw      0F22Bh		;adresa teplého startu (zde hl. smyèka monitoru)
f7be>1f 20     db      020h		;poèet znakù na øádek
f7bf>20 00ec   dw      0EC00h		;adresa zaèátku VIDEORAM
f7c1>22 0000   dw      0000h		;místo pro AF
f7c3>24 0000   dw      0000h		;místo pro BC
f7c5>26 0000   dw      0000h		;místo pro DE
f7c7>28 0000   dw      0000h		;místo pro HL
f7c9>2a a07f   dw      07FA0h		;místo pro SP
f7cb>2c 0010   dw      01000h		;místo pro PC

;odskoková tabulka pro pøerušení
;-------------------------------
f7cd c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FE0 - jp F7EC + FF  - pøer. 0
f7d1 c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FE4 - jp F7EC + FF  - pøer. 1
f7d5 c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FE8 - jp F7EC + FF  - pøer. 2
f7d9 c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FEC - jp F7EC + FF  - pøer. 3
f7dd c3ecf7ff  db      0c3h,0ech,0f7h,0ffh	;>7FF0 - jp F7EC + FF  - pøer. 4
f7e1 c392f7ff  db      0c3h,092h,0f7h,0ffh	;>7FF4 - jp F792 + FF  - pøer. 5 od klávesy BR
f7e5 c348f7ff  db      0c3h,048h,0f7h,0ffh	;>7FF8 - jp F748 + FF  - pøer. 6 50 Hz
f7e9 c3ecf7    db      0c3h,048h,0f7h		;>7FFC - jp F7EC       - pøer. 7 (16kHz)


;rutina zpracovávající standartnì nevyuité pøerušení
;povolí další pøerušení ihned vrátí zpìt
;-----------------------------------------------------------
f7ec f5        push    af		;uschovej AF
f7ed 3e20      ld      a,20h		;pøíkaz konec pøerušení
f7ef d388      out     (88h),a		;pro 8259
f7f1 fb        ei      			;povol pøerušení procesoru
f7f2 f1        pop     af		;obnov AF
f7f3 c9        ret     			;návrat z pøerušení

f7f4 ff        db      12x0FFh		;a do F7FF (nevyuité místo)


;tato èást se pøi resetu/startu pøipíná na 0000 !!
;0F800h je studenı start systému po resetu/zapnutí
;-------------------------------------------------

f800 c318f8    jp      0f818h		;RESET/Start
f803 c3aaf8    jp      0f8aah		;Znak z klávesnice do A s èekáním a pípnutím
f806 c3ebf6    jp      0f6ebh		;Vstup z dìrné pásky do A
f809 c307f0    jp      0f007h		;znak z C na obrazovku vèetnì øídících kódù
f80c c3d9f6    jp      0f6d9h		;Vıstup na dìrovaè z C
f80f c3c6f6    jp      0f6c6h		;Vıstup na tiskárnu z C
f812 c31ff5    jp      0f51fh		;Vstup 1 bajtu z magnetofonu do A
f815 c36bf5    jp      0f56bh		;Vıstup bajtu z C na magnetofon


;Inicializace po startu pokraèuje zde
;(u na F818h, ne od nuly)
;------------------------------------
f818 3e01      ld      a,01h		;pøepni EPROM monitoru obvodem 3212
f81a d380      out     (80h),a		;na adresu F800, od 0000 je RAM
f81c 210000    ld      hl,0000h         ;testuj RAM od 0
f81f 24        inc     h		;s krokem 256
f820 7e        ld      a,(hl)
f821 2f        cpl     
f822 77        ld      (hl),a
f823 be        cp      (hl)
f824 2f        cpl     
f825 77        ld      (hl),a
f826 ca1ff8    jp      z,0f81fh		;dokud lze zapsat pokraèuj v testu
f829 2b        dec     hl		;v HL adresa posledního bajtu RAM
f82a 7c        ld      a,h
f82b fee8      cp      0e8h		;bylo to E800 a více?
f82d da32f8    jp      c,0f832h		;pokraèuj pokud ne
f830 26e7      ld      h,0e7h		;maximum E7FF, od E800 je Video64 (+Video32) a Monitor
f832 220400    ld      (0004h),hl	;ulo do promìnné monitoru RAMTOP (0004)
f835 2ec2      ld      l,0c2h		;nastav SP o nìco níe (7FC2 pro 32 kB RAM)
f837 f9        ld      sp,hl
f838 e5        push    hl		;a ulo tuto hodnotu na vrchol zásobníku
f839 44        ld      b,h
f83a 0ee0      ld      c,0e0h		;nad zásobníkem (od 7FE0) jsou tabulky
f83c 21cdf7    ld      hl,0f7cdh	;skokù na podprogramy obsluhující pøerušení
f83f 11ecf7    ld      de,0f7ech
f842 cd47f2    call    0f247h		;kopíruj od HL po DE na BC
f845 011200    ld      bc,0012h		;nastav promìnné pro monitor (0012-002D)
f848 21b1f7    ld      hl,0f7b1h
f84b 11ccf7    ld      de,0f7cch
f84e cd47f2    call    0f247h		;kopíruj 
f851 3a00c0    ld      a,(0c000h)	;pokud není na (C000) hodnota 3E
f854 fe3e      cp      3eh		;tak není pøipojen modul kreslièe
f856 c281f8    jp      nz,0f881h	;a pokraèuj dále v inicializaci
f859 3e00      ld      a,00h		;nastavení kreslièe a jeho promìnnıch
f85b 327901    ld      (0179h),a	;promìnná kreslièe na adrese 0179 (pisátko)
f85e d3f4      out     (0f4h),a		;zvedni pisátko nahoru
f860 3eb6      ld      a,0b6h		;inicializuj kresliè
					;1 01 10 1 10 A,B-mode 1 
					;Port A,B input, C output
f862 d3f3      out     (0f3h),a		;F3 øídící port
f864 af        xor     a		;do A 0
f865 d3f2      out     (0f2h),a		;na port C
f867 3e10      ld      a,10h		;0001 0000
f869 d3f2      out     (0f2h),a		;na port C
f86b 3e32      ld      a,32h		;0 011 001 0 PC1=0
f86d d3f3      out     (0f3h),a		;na øídící port (nuluj bit1 C)
f86f 3e01      ld      a,01h
f871 d3f0      out     (0f0h),a		;??? port A je vstupní ????
f873 af        xor     a
f874 d3f0      out     (0f0h),a
f876 3e72      ld      a,72h
f878 d3f3      out     (0f3h),a
f87a 3e01      ld      a,01h
f87c d3f1      out     (0f1h),a		;??? port B je vstupní ????
f87e af        xor     a
f87f d3f1      out     (0f1h),a

f881 210300    ld      hl,0003h		;nastav I/O bajt na standartní hodnotu
f884 3669      ld      (hl),69h		;69 (vıstup - obrazovka, vstup - klávesnice, magnetofon)
f886 2100ec    ld      hl,0ec00h	;do HL adresa RAM Video32 (EC00)
f889 dbfe      in      a,(0feh)		;èti port FE (Video64)
f88b 3c        inc     a		;a pokud je tam 255
f88c ca96f8    jp      z,0f896h		;nemáme Video64 a skoè dál na nastavení Video32
f88f 3e40      ld      a,40h		;pro Video64 je 64 (40h) znakù na øádek
f891 321f00    ld      (001fh),a	;tak to nastavíme do syst. promìnné (001F)
f894 26e8      ld      h,0e8h		;a také nastavíme adresu E800
f896 222000    ld      (0020h),hl	;do systémové promìnné (0020) adresa Videoram
f899 3eb4      ld      a,0b4h		;inicializace 8255 modulu STAPER
f89b d3fb      out     (0fbh),a		;1 01 10 1 00 A,B-mode 1 
					;Port A input, B,C output
f89d 3e09      ld      a,09h		;0000 1001
f89f d3fb      out     (0fbh),a
f8a1 3e05      ld      a,05h		;0000 0101
f8a3 d3fb      out     (0fbh),a
f8a5 dbf8      in      a,(0f8h)
f8a7 c3b3f1    jp      0f1b3h		;pokraèuj inicializací øadièe pøerušení 8259

;Znak z klávesnice do A s èekáním a pípnutím
;-------------------------------------------
f8aa c5        push    bc		;úschova registrù
f8ab e5        push    hl
f8ac 3a0600    ld      a,(0006h)	;do A èasování klávesnice a blikání kurzoru (0006)
f8af e61f      and     1fh		;nech pouze spodních 5 bitù
f8b1 c2acf8    jp      nz,0f8ach	;a èekej dokud nejsou nulové - èeká urèenou dobu
f8b4 320700    ld      (0007h),a	;0 na (0007h) povolí blikání kurzoru
f8b7 cdc9f8    call    0f8c9h		;Znak z klávesnice do C (bez pauzy a bez píp)
f8ba d2b7f8    jp      nc,0f8b7h	;opakuj dokud se nestiskne klávesa
f8bd fe80      cp      80h		;pípni jinak pro znak 080h (END)
f8bf 210412    ld      hl,1204h
f8c2 ca79f9    jp      z,0f979h		;píp (parametry v HL)
f8c5 2c        inc     l		;a jinak pro ostatní klávesy
f8c6 c379f9    jp      0f979h		;píp (parametry v HL)

;Znak z klávesnice do C bez èekání (bez pípnutí)
;-----------------------------------------------
f8c9 f3        di      			;zaka pøerušení - mìníme nastavení 8255
f8ca 3e98      ld      a,98h		;pošli 1 00(Amod0) 11, 0(Bmod0) 00 - 
f8cc d387      out     (87h),a		;na øídící port 8255
f8ce db84      in      a,(84h)		;zjisti sloupec stisknuté klávesy
f8d0 cd67f9    call    0f967h		;vypoèítej  0-7 nebo nic (pozici prvního nulového bitu)
f8d3 3e8a      ld      a,8ah		;pošli 100 01 0 10 - obnov nastavení portu 84
f8d5 d387      out     (87h),a		;øídící port 8255
f8d7 fb        ei      			;povol pøerušení
f8d8 d0        ret     nc		;není nic stisknuto - návrat
f8d9 65        ld      h,l		;ulo do H sloupec stištìné klávesy (0-7)
f8da db85      in      a,(85h)		;zjisti v které øadì byla stištìná klávesa
f8dc cd67f9    call    0f967h		;vypoèítej 0-7 nebo nic
f8df d0        ret     nc		;není nic stisknuto - návrat
f8e0 7d        ld      a,l		;A=L - øada v které byla stištìna klávesa
f8e1 87        add     a,a		;násobíme 8
f8e2 87        add     a,a		;(posun o 3 bity doleva)
f8e3 87        add     a,a		;
f8e4 b4        or      h		;ORem pøidáme sloupec (spodní 3 bity)
					;tzn. 8x øada + sloupec
f8e5 211ef9    ld      hl,0f91eh	;tudy se bude vracet
f8e8 e5        push    hl		;(na zásobník)
f8e9 2127f9    ld      hl,0f927h	;tabulka  jednotlivıch kódù klávesnice v 8-cích
f8ec 85        add     a,l		;vypoèítej adresu znaku
f8ed 6f        ld      l,a		;do HL
f8ee 4e        ld      c,(hl)		;a do C vyzvedni kód stištìného znaku
f8ef 79        ld      a,c		;následuje pár testù - kód do A
f8f0 fe18      cp      18h		;byl to 18 - kurzor vpravo ?
f8f2 c2fbf8    jp      nz,0f8fbh	;pokud ne pokraèuj
f8f5 3a0b00    ld      a,(000bh)	;pokud ano, dej do A kód znaku na kterı ukazuje kurzor
f8f8 c31df9    jp      0f91dh		;a pokraèuj návratem s tím e tenhle znak bude v C

;vyhodnocení CTRL A SH
;---------------------
f8fb fe21      cp      21h		;pokud je to znak menší ne 21h
f8fd d8        ret     c		;tak se vra pøes 0F91E
f8fe db86      in      a,(86h)		;port C - horní 4 bity jsou tlaèítka FB FB CTRL SH
f900 2f        cpl     			;invertuj (1 pokud bylo stištìno)
f901 e630      and     30h		;zajímá nás pouze CTRL a SH
f903 c8        ret     z		;pokud nebylo vra se pøez 0F91E
f904 e610      and     10h		;je to SH?
f906 ca17f9    jp      z,0f917h		;skoè pokud to byl CTRL
;stisknuta klávesa a SH
;------------------------
f909 67        ld      h,a		;schovej si A (hodnota 0001 0000)
f90a 79        ld      a,c		;vyzvedni si kód znaku
f90b fe40      cp      40h		;pro znaky 33-63 se bude Xorovat (mìnit) bit 4
					;9=) - horní znaky nad nepísmenkama
f90d 7c        ld      a,h		;obnov schovanou hodnotu
f90e da12f9    jp      c,0f912h		;odskoè pro 33-63
f911 87        add     a,a		;pro znaky 64 a vıše se mìní bit 5
					;z velkıch udìlá malá písmenka
f912 a9        xor     c		;proveï zmìnu
f913 4f        ld      c,a		;ulo do C
f914 3e80      ld      a,80h		;v A 80 jako pøíznak kódu se shiftem
f916 c9        ret     			;a vra se pøes 0F91E
;stisknuta klávesa a CTRL
;------------------------
f917 79        ld      a,c		;vyzvedni si kód znaku
f918 fe40      cp      40h		;pro znaky 33-63 se
f91a d8        ret     c		;vra pøes 0F91E
f91b e61f      and     1fh		;jinak ponech pouze spodních 5 bitù (rozsah 0-31)
f91d 4f        ld      c,a		;vlo do C a vra se pøes
;tudy se vrací rutina Znak z klávesnice do C bez èekání
;provede nastavení èasování klávesnice a blikání kurzoru
;-------------------------------------------------------
f91e 210600    ld      hl,0006h		;HL = promìnná - èasování klávesnice a blikání kurzoru
f921 35        dec     (hl)		;testujeme jestli je 0
f922 34        inc     (hl)		;
f923 37        scf     			;nastav Carry 
f924 c0        ret     nz		;pro návrat pokud není 0
f925 34        inc     (hl)		;zvyš promìnnou (sma carry)
f926 c9        ret     			;vra se

;tabulka jednotlivıch kódù klávesnice v 8-cích
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

;rutina najde první nenulovı bit v A
;jeho èíslo (0-7) je v L
;pokud najde je nastaven carry
;-----------------------------------
f967 2e08      ld      l,08h		;jedeme po 8 bitù 
f969 2d        dec     l		;posuò se na další bit
f96a 3f        ccf     			;invertuj carry
f96b f8        ret     m		;návrat pøi L=255 - nenastaven ádnı bit na 0
f96c 07        rlca    			;posun na další bit
f96d da69f9    jp      c,0f969h		;pokud je jednièkovı opakuj
f970 c602      add     a,02h		;???
f972 c9        ret     			;návrat

;pípnutí s délkou 18 a vıškou 17
--------------------------------
f973 c5        push    bc		;uschovej registry
f974 e5        push    hl
f975 f3        di      			;zaka pøerušení
f976 2a1700    ld      hl,(0017h)	;vıška a délka tónu do HL
f979 d5        push    de		;uschovej i DE
f97a 5d        ld      e,l		;vıška do E (z L)
f97b 3e06      ld      a,06h		;0000 0110
;smyèka pøepínající vıstup na repro
f97d 53        ld      d,e		;vıšku do D z E kde byla uschovaná (doba mezi pøepnutím)
f97e d387      out     (87h),a		;0000 011 0 vıstup na repro - dá tam 0 (bit3 C)
					;pøi prvním prùchodu, pak se støídá 1-0-1-0-...
f980 2b        dec     hl		;256*h je doba trvání tónu (l zanedbává
f981 25        dec     h		;otestuj H na nulu
f982 24        inc     h
f983 ca8ff9    jp      z,0f98fh		;pokud ano konèíme
f986 15        dec     d		;èítaè pro vıšku (pauzy mezi pøepnutím z 0 na 1)
f987 c280f9    jp      nz,0f980h	;opakuj - èekání mezi pøenutím z 0-1 a naopak
f98a ee01      xor     01h		;vıstup na repro bude invertován (0-1 nebo 1-0)
f98c c37df9    jp      0f97dh		;a zpìt do smyèky generující pípnutí
;ukonèení píp	
f98f d1        pop     de		;obnov registry
f990 e1        pop     hl
f991 79        ld      a,c		;tato rutina nemìní obsah BC ...
					;take znak co byl v C do A
f992 c1        pop     bc
f993 fb        ei      			;povol pøerušení
f994 c9        ret     			;a návrat

;rolování textu na obrazovku je-li èíslo zobrazovaného øádku
;rovno èíslu na 0013 (délka stránky)
;-----------------------------------------------------------
f995 c5        push    bc		;uschovej registry BC a DE
f996 d5        push    de
f997 2a2000    ld      hl,(0020h)	;do HL adresa zaèátku VIDEORAM
f99a 5d        ld      e,l		;a zároveò i do DE
f99b 54        ld      d,h		;v DE adresa prvního øádku obrazovky
f99c 3a1f00    ld      a,(001fh)	;do A poèet znakù na øádek (32/64 standartnì)
f99f 6f        ld      l,a		;a dáme ho do L - v HL adresa 2 øádku obrazovky
f9a0 3a1300    ld      a,(0013h)	;do A délka stránky (poèet øádkù: 1-31)
f9a3 4f        ld      c,a		;dáme do C
f9a4 3a1f00    ld      a,(001fh)	;do A poèet znakù na øádek
f9a7 47        ld      b,a		;dáme do B
f9a8 7e        ld      a,(hl)		;zkopíruj znak z HL
f9a9 12        ld      (de),a		;na DE
f9aa 13        inc     de		;zvyš adresy
f9ab 23        inc     hl		;(posun na další znak)
f9ac 05        dec     b		;sni poèítadlo znakù na øádek
f9ad c2a8f9    jp      nz,0f9a8h	;a opakuj kopírování dokud øádek neskonèil
f9b0 0d        dec     c		;sni poèítadlo øádkù
f9b1 c2a4f9    jp      nz,0f9a4h	;opakuj dokud nejsou pøesunuty všechny øádky
f9b4 3a1f00    ld      a,(001fh)	;do A poèet znakù na øádek
f9b7 4f        ld      c,a		;do C
f9b8 3e20      ld      a,20h		;do A mezera
f9ba 12        ld      (de),a		;vyplò poslední øádek obrazovky mezerama
f9bb 13        inc     de		;posun na další adresu
f9bc 0d        dec     c		;dokud není celı øádek
f9bd c2baf9    jp      nz,0f9bah	;opakuj
f9c0 d1        pop     de		;obnov registry
f9c1 c1        pop     bc
f9c2 c9        ret     			;a návrat

f9c3           db      1596x0FFh	;a do konce pamìti (FFFF) jsou samé FF (volné místo)
