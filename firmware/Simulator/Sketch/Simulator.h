#pragma once

#include "Arduino.h"
#include "EEPROM.h"

#define __SIMULATOR__

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
