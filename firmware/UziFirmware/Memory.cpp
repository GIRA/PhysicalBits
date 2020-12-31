#include "Memory.h"

#define BUFFER_SIZE (sizeof(uint8*) * 500)

uint8 buf[BUFFER_SIZE];
uint8* cur = buf;


void* uzi_malloc(size_t size) 
{
	if (size == 0 || (cur + size) > (buf + BUFFER_SIZE)) 
	{
		return 0;
	}

	uint8* tmp = cur;
	cur += size;

	for (uint16 i = 0; i < size; i++) 
	{
		tmp[i] = 0;
	}
	return tmp;
}

void uzi_memreset()
{
	cur = buf;
}

uint16 uzi_available() 
{
	return BUFFER_SIZE - (cur - buf);
}

uint16 uzi_used() 
{
	return cur - buf;
}


/******************************STACK******************************/

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
float elements[MAX_SIZE];
int16 pointer = MAX_SIZE;

void stack_push(float element, Error& error)
{
	if (pointer <= 0)
	{
		error = STACK_OVERFLOW;
		return;
	}
	elements[--pointer] = element;
}

void stack_restoreFrom(float* source, uint16 size, Error& error)
{
	pointer = MAX_SIZE - size;
	error = NO_ERROR;
	if (size == 0) return;
	memcpy(&elements[pointer], source, size * sizeof(float));
}

void stack_saveTo(float* dest)
{
	size_t size = (MAX_SIZE - pointer) * sizeof(float);
	memcpy(dest, &elements[pointer], size);
}

float stack_pop(Error& error)
{
	if (pointer >= MAX_SIZE)
	{
		error = STACK_UNDERFLOW;
		return 0;
	}
	return elements[pointer++];
}

void stack_discard(uint16 amount, Error& error)
{
	pointer += amount;
	if (pointer > MAX_SIZE) 
	{
		pointer = MAX_SIZE;
		error = STACK_UNDERFLOW;
	}
}

float stack_top(void)
{
	return elements[pointer];
}

void stack_reset()
{
	pointer = MAX_SIZE;
}

uint16 stack_size() 
{
	return MAX_SIZE - pointer;
}

uint16 stack_getPointer()
{
	// TODO(Richo): For now, I'm making it look like the stack grows upwards
	return MAX_SIZE - pointer;
}

float stack_getElementAt(uint16 index, Error& error)
{
	index = MAX_SIZE - index - 1;
	if (index >= MAX_SIZE)
	{
		error = STACK_ACCESS_VIOLATION;
		return 0;
	}
	return elements[index];
}

void stack_setElementAt(uint16 index, float value, Error& error)
{
	index = MAX_SIZE - index - 1;
	if (index >= MAX_SIZE)
	{
		error = STACK_ACCESS_VIOLATION;
		return;
	}
	elements[index] = value;
}