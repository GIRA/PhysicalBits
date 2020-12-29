#include "ArrayReader.h"

ArrayReader::ArrayReader(uint8 * anArray, int16 size)
{
	position = 0;
	elements = anArray;
	size = size;
}

bool ArrayReader::isClosed(void)
{
	return position >= size;
}

uint8 ArrayReader::next(bool& timeout)
{
	timeout = false;
	counter++;
	return elements[position++];
}

int16 ArrayReader::getPosition(void)
{
	return position;
}