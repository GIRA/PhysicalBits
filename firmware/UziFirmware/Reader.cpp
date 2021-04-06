#include "Reader.h"

int32 Reader::nextLong(int16 size, bool& timeout)
{
	int32 result = 0;	
	for (int32 i = size - 1; i >= 0; i--)
	{
		result |= ((uint32)next(timeout) << (i * 8));
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
	uint32 a = next(timeout);
	if (timeout) return 0;
	uint32 b = next(timeout);
	if (timeout) return 0;
	uint32 c = next(timeout);
	if (timeout) return 0;
	uint32 d = next(timeout);
	if (timeout) return 0;

	uint32 value = (a << 24) | (b << 16) | (c << 8) | d;
	return uint32_to_float(value);
}

void Reader::resetCounter() 
{
	counter = 0;
}