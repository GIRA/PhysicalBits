#pragma once

#include "EEPROMReader.h"
#include "EEPROMWriter.h"

class EEPROMWearLevelingWriter
{

public:
	EEPROMWearLevelingWriter();
	~EEPROMWearLevelingWriter();
	
	void nextPut(unsigned char);
	bool atEnd(void);
	void close();

private:

	EEPROMWriter * writer;
	int beginPosition;
	bool closed = false;

	int findPosition(void);
	void escapeIfNecessary(unsigned char);
};

