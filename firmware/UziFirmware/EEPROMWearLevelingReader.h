#pragma once

#include "Reader.h"
#include "EEPROMReader.h"

class EEPROMWearLevelingReader : public Reader
{

public:
	EEPROMWearLevelingReader();
	
	uint8 next(void);
	uint8 next(bool&);

	bool atEnd(void);

private:

	EEPROMReader reader;
	int16 endPosition;

	int16 findPosition(void);
	uint8 escapeIfNecessary(uint8);
};

