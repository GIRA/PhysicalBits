#pragma once

#include "Reader.h"
#include "EEPROMReader.h"

#define BEGIN_MARK	0xCE
#define END_MARK	0xCF

class EEPROMWearLevelingReader : public Reader
{

public:
	EEPROMWearLevelingReader();
	~EEPROMWearLevelingReader();
	
	unsigned char next(void);
	bool atEnd(void);

private:

	EEPROMReader * reader;
	int endPosition;

	int findPosition(void);
	unsigned char escapeIfNecessary(unsigned char);
};

