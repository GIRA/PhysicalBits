#pragma once

#include "EEPROM.h"

// E2END = The last EEPROM address
#define EEPROM_SIZE			(E2END + 1)

class EEPROMWriter
{

public:
	EEPROMWriter();
	EEPROMWriter(int);
	~EEPROMWriter(void) {}

	void nextPut(unsigned char);

private:
	int position;

	void incrementPosition(void);

};

