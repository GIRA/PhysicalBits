#pragma once

#include "Reader.h"

class ArrayReader : public Reader
{

public:
	ArrayReader(uint8*, int16);
	~ArrayReader(void) {}

	bool isClosed(void);

	uint8 next(bool&);

	int16 getPosition(void);
private:

	int16 position;
	int16 size;
	uint8 * elements;
};

