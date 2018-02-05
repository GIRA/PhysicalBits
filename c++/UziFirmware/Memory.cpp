#include "Memory.h"

#define BUFFER_SIZE (sizeof(uint8*) * 500)

uint8 buf[BUFFER_SIZE];
uint8* cur = buf;


void* uzi_malloc(size_t size) 
{
	if ((cur + size) > (buf + BUFFER_SIZE)) 
	{
		return 0;
	}

	uint8* tmp = cur;
	cur += size;
	return tmp;
}

void uzi_memreset()
{
	cur = buf;
}