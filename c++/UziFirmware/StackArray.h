#pragma once

#include "types.h"

const uint16 MAX_SIZE = 100;

class StackArray
{

public:
	void push(float);
	float pop(void);
	float top(void);
	void reset(void);
	bool overflow(void);

private:

	float elements[MAX_SIZE];
	uint16 pointer = 0;

};

