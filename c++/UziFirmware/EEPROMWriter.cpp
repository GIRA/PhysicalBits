
#include "EEPROMWriter.h"

void EEPROMWriter::nextPut(unsigned char value) {
	EEPROM.write(_position, value);
	_position++;
}

