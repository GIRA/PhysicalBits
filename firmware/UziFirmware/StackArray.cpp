#include "StackArray.h"

void StackArray::push(float element)
{
	if (pointer >= MAX_SIZE)
	{
		error = STACK_OVERFLOW;
		return;
	}
	elements[pointer++] = element;
	if (pointer > max_depth) { max_depth = pointer; }
}

void StackArray::copyFrom(float* source, uint16 size)
{
	pointer = size;
	error = NO_ERROR;
	if (size == 0) return;
	memcpy(&elements[0], source, size * sizeof(float));
	if (pointer > max_depth) { max_depth = pointer; }
}

void StackArray::copyTo(float* dest)
{
	memcpy(dest, &elements[0], pointer * sizeof(float));
}

float StackArray::pop(void)
{
	if (pointer <= 0)
	{
		error = STACK_UNDERFLOW;
		return 0;
	}
	return elements[--pointer];
}

void StackArray::discard(uint16 amount) 
{
	pointer -= amount;
	if (pointer < 0) 
	{
		pointer = 0;
		error = STACK_UNDERFLOW;
	}
}

float StackArray::top(void)
{
	return elements[pointer - 1];
}

void StackArray::clear(void)
{
	pointer = 0;
	error = NO_ERROR;
}

bool StackArray::hasError(void)
{
	return error != NO_ERROR;
}

Error StackArray::getError(void)
{
	return error;
}

uint16 StackArray::getPointer(void)
{
	return pointer;
}

float StackArray::getElementAt(uint16 index)
{
	if (index >= MAX_SIZE)
	{
		error = STACK_ACCESS_VIOLATION;
		return 0;
	}
	return elements[index];
}

void StackArray::setElementAt(uint16 index, float value)
{
	if (index >= MAX_SIZE)
	{
		error = STACK_ACCESS_VIOLATION;
		return;
	}
	elements[index] = value;
}

uint32 StackArray::available() 
{
	return (MAX_SIZE - max_depth) * sizeof(float);
}