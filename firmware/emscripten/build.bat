@echo off
set sim=../Simulator/Sketch
set sim_files=%sim%/Arduino.cpp %sim%/EEPROM.cpp %sim%/Servo.cpp %sim%/Simulator.cpp
set uzi=../UziFirmware
set uzi_files=%uzi%/ArrayReader.cpp %uzi%/Coroutine.cpp %uzi%/EEPROMReader.cpp %uzi%/EEPROMWearLevelingReader.cpp %uzi%/EEPROMWearLevelingWriter.cpp^
 %uzi%/EEPROMWriter.cpp %uzi%/GPIO.cpp %uzi%/Instruction.cpp %uzi%/Memory.cpp %uzi%/Monitor.cpp %uzi%/Program.cpp %uzi%/Reader.cpp %uzi%/Script.cpp^
 %uzi%/SerialReader.cpp %uzi%/UziSerial.cpp %uzi%/VM.cpp -x c++ %uzi%/UziFirmware.ino

set src=main.c %sim_files% %uzi_files%

set opts=-I %sim% -I %uzi% -fdeclspec -s LLD_REPORT_UNDEFINED

set exports=_GPIO_getPinValue,_GPIO_setPinValue,_Sketch_setup,_Sketch_loop,_Serial_readInto,_Serial_write,_Sketch_getMillis,_Sketch_setMillis,_EEPROM_read,_EEPROM_write,_EEPROM_size,_Stats_usedMemory,_Stats_coroutineResizeCounter

REM mkdir out 2> NUL
REM set out=out/simulator.js
set out=../../gui/ide/simulator.js

@echo on
em++ %opts% %src% -o %out% -s EXPORTED_FUNCTIONS=%exports%,_main,_malloc,_free -s EXPORTED_RUNTIME_METHODS=ccall,cwrap --post-js bindings.js
