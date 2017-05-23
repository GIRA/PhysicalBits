#pragma once

#include "types.h"
#include "Errors.h"

const uint16 MAX_SIZE = 100;

class StackArray
{

public:
	void push(float);
	float pop(void);
	float top(void);
	void reset(void);
	uint16 getPointer(void);
	void setPointer(uint16);
	float getElementAt(uint16);
	void setElementAt(uint16, float);
	bool hasError(void);
	Error getError(void);

private:

	float elements[MAX_SIZE];
	uint16 pointer = 0;
	Error error = NO_ERROR;
};

