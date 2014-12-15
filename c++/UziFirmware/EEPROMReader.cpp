
#include "EEPROMReader.h"

unsigned char EEPROMReader::nextChar(void) {
	return EEPROM.read(_position++);
}