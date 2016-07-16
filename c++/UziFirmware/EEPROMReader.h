#pragma once

#include "EEPROM.h"

#include "Reader.h"

// E2END = The last EEPROM address
#define EEPROM_SIZE			(E2END + 1)

class EEPROMReader : public Reader
{

public:
	EEPROMReader(void);
	EEPROMReader(int);
	~EEPROMReader(void) {}

	unsigned char nextChar(void);

private:
	int position;

	void incrementPosition(void);

};