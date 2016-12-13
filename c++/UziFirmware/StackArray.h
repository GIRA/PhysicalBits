#pragma once

#include "types.h"

const uint16 MAX_SIZE = 10;

class StackArray
{

public:
	void push(float);
	float pop(void);
	float top(void);
	void reset(void);
	bool overflow(void);
	uint16 getPointer(void);
	float getElementAt(uint16);

private:

	float elements[MAX_SIZE];
	uint16 pointer = 0;

};

