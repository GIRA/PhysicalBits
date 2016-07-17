#pragma once

#include "EEPROMReader.h"
#include "EEPROMWriter.h"

#define BEGIN_MARK	0xCE
#define END_MARK	0xCF

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

