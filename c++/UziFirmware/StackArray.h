#pragma once

#include "types.h"

const uint16 MAX_SIZE = 100;

enum StackError
{
	STACK_NO_ERROR, 
	STACK_OVERFLOW, 
	STACK_UNDERFLOW, 
	STACK_ACCESS_VIOLATION
};

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
	StackError getError(void);

private:

	float elements[MAX_SIZE];
	uint16 pointer = 0;
	StackError error = STACK_NO_ERROR;
};

