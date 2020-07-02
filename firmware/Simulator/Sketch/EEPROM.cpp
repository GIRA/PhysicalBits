
#include "EEPROM.h"


unsigned char EEPROMClass::read(int address)
{
	return bytes[address];
}

void EEPROMClass::write(int address, unsigned char value)
{
	bytes[address] = value;
}

EEPROMClass EEPROM;