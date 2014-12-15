#pragma once

#include <string.h>

class ReadStream {

public:
	
	virtual unsigned char nextChar(void) = 0;
		
	virtual unsigned char * upTo(unsigned char, bool);
	virtual long nextLong(int);

};

