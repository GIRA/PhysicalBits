#pragma once

#include "types.h"
#include "Errors.h"
#include <Arduino.h>

/*
TODO(Richo): Maybe make the stack array size variable and use the same memory 
space as the program? I should probably track the stack size as well to see
how much memory we're normally wasting by having a fixed size. Also, I should
be careful if I make the stack and the program share the same memory space 
because the program might need to grow as well (due to the coroutine resizings).
The easiest implementation would probably be to make the stack grow backwards.
In any case, I must be careful to avoid allocating memory used by the stack.
*/
const uint16 MAX_SIZE = 100;

class StackArray
{

public:
	void push(float);
	float pop(void);
	float top(void);
	void clear(void);
	uint16 getPointer(void);
	float getElementAt(uint16);
	void setElementAt(uint16, float);
	bool hasError(void);
	Error getError(void);
	void copyFrom(float*, uint16);
	void copyTo(float*);
	void discard(uint16);

	uint32 available(void);
	uint16 max_depth = 0;
private:

	float elements[MAX_SIZE];
	uint16 pointer = 0;
	Error error = NO_ERROR;
};
