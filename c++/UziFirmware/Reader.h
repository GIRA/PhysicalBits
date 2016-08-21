#pragma once

#include <string.h>

class Reader
{

public:

	virtual unsigned char next(bool&) = 0;

	virtual unsigned char * next(int, bool&);
	virtual unsigned char * upTo(unsigned char, bool, bool&);
	virtual long nextLong(int, bool&);
	virtual float nextFloat(bool&);
};

