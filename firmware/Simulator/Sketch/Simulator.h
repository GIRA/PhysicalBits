#pragma once

#include "Arduino.h"
#include "EEPROM.h"

#define __SIMULATOR__

#ifdef __EMSCRIPTEN__
#define EXTERN
#else
#define EXTERN extern "C" __declspec(dllexport)
#endif 

// The following macros will work for arduino UNO, other versions should redefine.
#define __TOTAL_PINS__												 20
#define __IS_ANALOG__(x)						((x) >= 14 && (x) <= 19)
#define __IS_DIGITAL__(x)							 (!__IS_ANALOG__(x))

unsigned short __getPinValue(unsigned int);
void __setPinValue(unsigned int, unsigned short);

struct RuntimeStats
{
	uint16_t usedMemory;
	uint32_t coroutineResizeCounter;
};
extern RuntimeStats Stats;

EXTERN unsigned short GPIO_getPinValue(unsigned int pin);
EXTERN void GPIO_setPinValue(unsigned int pin, unsigned short value);
EXTERN void Sketch_setup(void);
EXTERN void Sketch_loop(void);
EXTERN size_t Serial_readInto(char* buffer, size_t len);
EXTERN void Serial_write(char* str, size_t len);
EXTERN long Sketch_getMillis(void);
EXTERN void Sketch_setMillis(long millis);
EXTERN unsigned char EEPROM_read(int address);
EXTERN void EEPROM_write(int address, unsigned char value);
EXTERN int EEPROM_size(void);
EXTERN uint16_t Stats_usedMemory();
EXTERN uint32_t Stats_coroutineResizeCounter();