#include "Memory.h"

#define BUFFER_SIZE (sizeof(uint8*) * 700)

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

float* stack_top = (float*)(buf + BUFFER_SIZE);

void stack_push(float element, Error& error)
{
	if (stack_top <= (float*)buf)
	{
		error = STACK_OVERFLOW;
		return;
	}
	stack_top -= 1;
	*stack_top = element;
}

float stack_pop(Error& error)
{
	if (stack_top >= (float*)(buf + BUFFER_SIZE))
	{
		error = STACK_UNDERFLOW;
		return 0;
	}
	float value = *stack_top;
	stack_top += 1;
	return value;
}

void stack_reset()
{
	stack_top = (float*)(buf + BUFFER_SIZE);
}

void stack_discard(uint16 amount, Error& error)
{
	stack_top += amount;
	if (stack_top > (float*)(buf + BUFFER_SIZE))
	{
		stack_reset();
		error = STACK_UNDERFLOW;
	}
}

uint16 stack_size()
{
	return (float*)(buf + BUFFER_SIZE) - stack_top;
}

uint16 stack_getPointer()
{
	// TODO(Richo): For now, I'm making it look like the stack grows upwards
	return stack_size();
}

float stack_getElementAt(uint16 index, Error& error)
{
	float* pointer = (float*)(buf + BUFFER_SIZE) - index - 1;
	if (pointer < stack_top)
	{
		error = STACK_ACCESS_VIOLATION;
		return 0;
	}
	return *pointer;
}


void stack_setElementAt(uint16 index, float value, Error& error)
{
	float* pointer = (float*)(buf + BUFFER_SIZE) - index - 1;
	if (pointer < stack_top)
	{
		error = STACK_ACCESS_VIOLATION;
		return;
	}
	*pointer = value;
}

void stack_restoreFrom(float* source, uint16 size, Error& error)
{
	stack_top = (float*)(buf + BUFFER_SIZE) - size;
	error = NO_ERROR;
	if (size == 0) return;
	memcpy(stack_top, source, size * sizeof(float));
}

void stack_saveTo(float* dest)
{
	size_t size = (buf + BUFFER_SIZE) - (uint8*)stack_top;
	memcpy(dest, stack_top, size);
}