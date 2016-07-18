#pragma once

#include "EEPROMUtils.h"

class EEPROMWriter
{

public:
	EEPROMWriter();
	EEPROMWriter(int);
	~EEPROMWriter(void) {}

	void nextPut(unsigned char);
	int getPosition(void);

private:
	int position;

	void incrementPosition(void);

};

