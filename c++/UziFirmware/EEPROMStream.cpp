
#include "EEPROMStream.h"

unsigned char EEPROMStream::nextChar(void) {
	unsigned char value = EEPROM.read(_position);
	_position++;
	return value;
}