#pragma once

#include "EEPROM.h"

// E2END = The last EEPROM address
#define EEPROM_SIZE			(E2END + 1)

// Wear leveling delimiter constants
#define EEPROM_BEGIN_MARK	0xCE
#define EEPROM_END_MARK	0xCF

// Taken from: http://stackoverflow.com/a/14997413
inline int positive_modulo(int i, int n)
{
	return (i % n + n) % n;
}