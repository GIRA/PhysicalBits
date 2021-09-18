@echo off
set sim=../Simulator/Sketch
set src=main.c %sim%/Arduino.cpp %sim%/EEPROM.cpp %sim%/Servo.cpp %sim%/Simulator.cpp

set opts=-I %sim% -fdeclspec -s LLD_REPORT_UNDEFINED

mkdir out 2> NUL
em++ %opts% %src% -o out/main.js && node out/main.js
