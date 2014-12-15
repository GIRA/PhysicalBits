#pragma once

#include "EEPROM.h"

class EEPROMWriter {

public:
	EEPROMWriter() {
		_position = 0;
	}
	~EEPROMWriter(void) {}
		
	void nextPut(unsigned char);
	
private:

	int _position;
};

