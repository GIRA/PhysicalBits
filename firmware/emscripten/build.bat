@echo off
set sim=../Simulator/Sketch
set sim_files=%sim%/Arduino.cpp %sim%/EEPROM.cpp %sim%/Servo.cpp %sim%/Simulator.cpp
set uzi=../UziFirmware
set uzi_files=%uzi%/ArrayReader.cpp %uzi%/Coroutine.cpp %uzi%/EEPROMReader.cpp %uzi%/EEPROMWearLevelingReader.cpp %uzi%/EEPROMWearLevelingWriter.cpp^
 %uzi%/EEPROMWriter.cpp %uzi%/GPIO.cpp %uzi%/Instruction.cpp %uzi%/Memory.cpp %uzi%/Monitor.cpp %uzi%/Program.cpp %uzi%/Reader.cpp %uzi%/Script.cpp^
 %uzi%/SerialReader.cpp %uzi%/UziSerial.cpp %uzi%/VM.cpp -x c++ %uzi%/UziFirmware.ino

set src=main.c %sim_files% %uzi_files%

set opts=-I %sim% -I %uzi% -fdeclspec -s LLD_REPORT_UNDEFINED

mkdir out 2> NUL
em++ %opts% %src% -o out/main.js && node out/main.js
