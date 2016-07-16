#pragma once

#include <string.h>

class Reader
{

public:

	virtual unsigned char next(void) = 0;

	virtual unsigned char * upTo(unsigned char, bool);
	virtual long nextLong(int);

};

