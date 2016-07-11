#pragma once

#include "EEPROM.h"

// E2END = The last EEPROM address
#define EEPROM_SIZE			(E2END + 1)

class EEPROMWriter
{

public:
	EEPROMWriter();
	~EEPROMWriter(void) {}

	void nextPut(unsigned char);

private:

	int _position;
	int _offset;
	int _nextPointer;

	int findOffset(void);
	int actualIndex(void);
	void incrementPosition(void);
	void fixPointers(void);
	void jumpOverLastProgram(void);

};

