#pragma once

#include "EEPROMUtils.h"

class EEPROMWriter
{

public:
	void nextPut(uint8);
	int16 getPosition(void);
	void setPosition(int16);

private:
	int16 position = 0;

	void incrementPosition(void);

};

