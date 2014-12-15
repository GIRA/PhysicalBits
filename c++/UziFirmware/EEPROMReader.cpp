
#include "EEPROMReader.h"

unsigned char EEPROMReader::nextChar(void) {
	unsigned char value = EEPROM.read(_position);
	_position++;
	return value;
}