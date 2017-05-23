#include "StackArray.h"


void StackArray::push(float element)
{
	if (pointer >= MAX_SIZE)
	{
		error = STACK_OVERFLOW;
		return;
	}
	elements[pointer++] = element;
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

float StackArray::top(void)
{
	return elements[pointer - 1];
}

void StackArray::reset(void)
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

void StackArray::setPointer(uint16 value)
{
	pointer = value;
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