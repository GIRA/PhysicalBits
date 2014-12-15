#pragma once

#include "EEPROM.h"
#include "Reader.h"

class EEPROMReader : public Reader {

public:
	EEPROMReader() {
		_position = 0;
	}
	~EEPROMReader(void) {}
	
	unsigned char nextChar(void);

private:

	int _position;
	
};