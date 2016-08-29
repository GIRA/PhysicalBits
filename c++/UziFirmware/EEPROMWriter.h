#pragma once

#include "EEPROMUtils.h"

class EEPROMWriter
{

public:
	EEPROMWriter();
	EEPROMWriter(int16);
	~EEPROMWriter(void) {}

	void nextPut(uint8);
	int16 getPosition(void);

private:
	int16 position;

	void incrementPosition(void);

};

