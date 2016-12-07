#include "StackArray.h"


void StackArray::push(float element)
{
	elements[pointer++] = element;
}

float StackArray::pop(void)
{
	return elements[--pointer];
}

float StackArray::top(void)
{
	return elements[pointer - 1];
}

void StackArray::reset(void)
{
	pointer = 0;
}

bool StackArray::overflow(void)
{
	return pointer >= MAX_SIZE;
}

StackArray* StackArray::copy(void)
{
	StackArray* result = new StackArray();
	for (int i = 0; i < pointer; i++)
	{
		result->push(elements[i]);
	}
	return result;
}