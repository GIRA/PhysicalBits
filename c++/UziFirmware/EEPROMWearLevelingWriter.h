#pragma once

#include "EEPROMReader.h"
#include "EEPROMWriter.h"

class EEPROMWearLevelingWriter
{

public:
	EEPROMWearLevelingWriter();
	~EEPROMWearLevelingWriter();
	
	void nextPut(uint8);
	bool atEnd(void);
	void close();

private:

	EEPROMWriter writer;
	int16 beginPosition;
	bool closed = false;

	int16 findPosition(void);
	void escapeIfNecessary(uint8);
};

