#include "ArrayReader.h"

ArrayReader::ArrayReader(unsigned char * anArray, int size)
{
	position = 0;
	elements = anArray;
	size = size;
}

bool ArrayReader::isClosed(void)
{
	return position >= size;
}

unsigned char ArrayReader::next(bool& timeout)
{
	timeout = false;
	return elements[position++];
}

int ArrayReader::getPosition(void)
{
	return position;
}

unsigned char * ArrayReader::upTo(unsigned char aCharacter, bool inclusive, bool& timeout)
{
	int arraySize = remaining();
	unsigned char * result = new unsigned char[arraySize];
	int i = 0;
	bool found = false;
	while (i < arraySize && !found)
	{
		unsigned char nextChar = next(timeout);
		found = (nextChar == aCharacter) || timeout;
		if (!found || inclusive)
		{
			result[i] = nextChar;
			i++;
		}
	}
	if (i < arraySize)
	{
		unsigned char * temp = new unsigned char[i];
		memcpy(temp, result, i);
		delete[] result;
		result = temp;
	}
	return result;
}

int ArrayReader::remaining(void)
{
	return size - position;
}