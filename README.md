Emulator of Czech 80s' school computer IQ-151 written in Java

feel free to use it for free... GPL3

Omikron


Features:
=========

* Basic 6, Basic G
* Video 32, Video 64
* Grafik
* Amos (both Assembler and Pascal)
* Debugger (both 8080 Z80 mnemonics)
* SD-Rom module
* Disk2 (Floppy) module


---

## Formát .isn, verze 1

Raw binární stream, bez komprese, vícebajtové hodnoty little-endian — stejně jako `.osn`.

| Offset | Velikost | Obsah |
|---|---|---|
| 0 | 3 | signatura `'I','S','N'` |
| 3 | 1 | verze = 1 |
| 4 | 10 | konfigurace: `main`, `monit`, `video`, `mem64`, `grafik`, `sdrom`, `disc2`, `audio`, `smartkeyboard`, `V64ena32` |
| 14 | 16 | CPU: A, F, B, C, D, E, H, L, SP(lo,hi), PC(lo,hi), MEMPTR(lo,hi), regSim, příznakový bajt |
| 30 | 6 | stroj: `port80`, bootstrap, amosBank (`0xFF` = nepřepnuto), disc2mounted, páskový příznakový bajt, rezerva |
| 36 | 5 | 8255: výstup `pio.getState()` — CWR, PC, PB, PA, inte |
| 41 | 11 | 8259: icw1–4, ocw1–3, irr, isr, `state.ordinal()`, `intack.ordinal()` |
| 52 | 1 | grafikPresent |
| 53 | 4 | *jen s Grafikem:* D0, D1, D2, příznaky (Enabled/ShowGR/BitAcces/PenOn) |
| 57 | 16384 | *jen s Grafikem:* `GrafRam[64][256]` |
| … | 65536 | RAM `Ram[0..63]` po 1024 B |
| … | 2048 | VRAM `VRam[0..1]` po 1024 B |

Příznakový bajt CPU: bit0 `pendingEI`, bit1 `activeINT`, bit2 `halted`.
Páskový příznakový bajt: bit0 `khz1`, bit1 `tapein`, bit2 `tapeo`, bit3 `tapeout`, bit4 `tapestart`, bit5 `tapeinv`.

Velikost souboru: 84 025 B s Grafikem, 67 637 B bez něj.
