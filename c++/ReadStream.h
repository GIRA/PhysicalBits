#pragma once

#include <string.h>

class ReadStream {

public:
		
	virtual unsigned char nextChar(void) = 0;
		
	virtual unsigned char * upTo(unsigned char, bool) = 0;

	virtual long nextLong(int);

};

