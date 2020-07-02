#pragma once

#include "Reader.h"
#include "EEPROMUtils.h"

class EEPROMReader : public Reader
{

public:
	uint8 next(bool&);

	uint8 next(void);
	uint8 peek(void);
	uint8 back(void);
	uint8 peekBack(void);
	int16 getPosition();
	void setPosition(int16);

private:
	int16 position = 0;

	void incrementPosition(void);
	void decrementPosition(void);
};