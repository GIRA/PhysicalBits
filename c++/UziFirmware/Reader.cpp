#include "Reader.h"

long Reader::nextLong(int size, bool& timeout)
{
	long result = 0;	
	for (int i = size - 1; i >= 0; i--)
	{
		result |= ((unsigned long)next(timeout) << (i * 8));
		if (timeout) return 0;
	}
	return result;
}

/*
INFO(Richo): Code taken and adapted from
http://www.microchip.com/forums/m590535.aspx#590570
*/
float Reader::nextFloat(bool& timeout)
{
	union
	{
		float f;
		unsigned long ul;
	} u;
	unsigned long a = next(timeout);
	if (timeout) return 0;
	unsigned long b = next(timeout);
	if (timeout) return 0;
	unsigned long c = next(timeout);
	if (timeout) return 0;
	unsigned long d = next(timeout);
	if (timeout) return 0;

	u.ul = (a << 24) | (b << 16) | (c << 8) | d;
	return u.f;
}

unsigned char * Reader::next(int size, bool& timeout)
{
	unsigned char * result = new unsigned char[size];
	for (int i = 0; i < size; i++)
	{
		result[i] = next(timeout);
		if (timeout) return 0;
	}
	return result;
}

unsigned char * Reader::upTo(unsigned char aCharacter, bool inclusive, bool& timeout)
{
	// This number should be big enough to prevent too many resizings 
	// and small enough to avoid wasting memory. For now, I'll choose 100 bytes.
	int arraySize = 100;
	unsigned char * result = new unsigned char[arraySize];
	int i = 0;
	bool found = false;
	while (!found)
	{
		unsigned char nextChar = next(timeout);
		found = (nextChar == aCharacter) || timeout;
		if (!found || inclusive)
		{
			result[i] = nextChar;
			i++;
			// If we reached the end of the array, we need to resize it.
			if (i >= arraySize)
			{
				int newSize = arraySize * 2;
				unsigned char * temp = new unsigned char[newSize];
				memcpy(temp, result, arraySize);
				delete[] result;
				result = temp;
				arraySize = newSize;
			}
		}
	}
	// If the resulting array is smaller than our expectation, we need to resize it.
	if (i < arraySize)
	{
		unsigned char * temp = new unsigned char[i];
		memcpy(temp, result, i);
		delete[] result;
		result = temp;
	}
	return result;
}

