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
	return elements[position++];
}

int16 ArrayReader::getPosition(void)
{
	return position;
}

uint8 * ArrayReader::upTo(uint8 aCharacter, bool inclusive, bool& timeout)
{
	int16 arraySize = remaining();
	uint8 * result = new uint8[arraySize];
	int16 i = 0;
	bool found = false;
	while (i < arraySize && !found)
	{
		uint8 nextChar = next(timeout);
		found = (nextChar == aCharacter) || timeout;
		if (!found || inclusive)
		{
			result[i] = nextChar;
			i++;
		}
	}
	if (i < arraySize)
	{
		uint8 * temp = new uint8[i];
		memcpy(temp, result, i);
		delete[] result;
		result = temp;
	}
	return result;
}

int16 ArrayReader::remaining(void)
{
	return size - position;
}