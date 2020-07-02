#pragma once

#include "EEPROM.h"
#include "types.h"

// E2END = The last EEPROM address
#define EEPROM_SIZE			(E2END + 1)

// Wear leveling delimiter constants
#define EEPROM_BEGIN_MARK	0xCE
#define EEPROM_END_MARK	0xCF

// Taken from: http://stackoverflow.com/a/14997413
inline int16 positive_modulo(int16 i, int16 n)
{
	return (i % n + n) % n;
}