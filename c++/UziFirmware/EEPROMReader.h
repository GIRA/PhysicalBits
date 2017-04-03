#pragma once

#include "Reader.h"
#include "EEPROMUtils.h"

class EEPROMReader : public Reader
{

public:
	EEPROMReader(void);
	EEPROMReader(int16);
	virtual ~EEPROMReader(void) {}

	uint8 next(bool&);

	uint8 next(void);
	uint8 peek(void);
	uint8 back(void);
	uint8 peekBack(void);
	int16 getPosition();

private:
	int16 position;

	void incrementPosition(void);
	void decrementPosition(void);
};