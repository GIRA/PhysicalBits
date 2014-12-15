#pragma once

#include "EEPROM.h"
#include "ReadStream.h"

class EEPROMStream : public ReadStream {

public:
	EEPROMStream() {
		_position = 0;
	}
	~EEPROMStream(void) {}
	
	unsigned char nextChar(void);

private:

	int _position;
	
};