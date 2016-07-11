#pragma once

#include "EEPROM.h"

#include "Reader.h"

// E2END = The last EEPROM address
#define EEPROM_SIZE			(E2END + 1)

class EEPROMReader : public Reader
{

public:
	EEPROMReader(void);
	~EEPROMReader(void) {}

	unsigned char nextChar(void);

private:

	int _position;
	int _offset;
	int _nextPointer;

	int findOffset(void);
	int actualIndex(void);
	void incrementPosition(void);

};