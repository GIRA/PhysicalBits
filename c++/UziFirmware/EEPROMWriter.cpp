
#include "EEPROMWriter.h"

void EEPROMWriter::nextPut(unsigned char value) {
	if (EEPROM.read(_position) != value) {
		EEPROM.write(_position, value);
	}
	_position++;
}

