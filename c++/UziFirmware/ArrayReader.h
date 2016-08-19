#pragma once

#include "Reader.h"

class ArrayReader : public Reader
{

public:
	ArrayReader(unsigned char*, int);
	~ArrayReader(void) {}

	bool isClosed(void);

	unsigned char next(bool&);

	int getPosition(void);

	unsigned char * upTo(unsigned char, bool, bool&);

private:

	int position;
	int size;
	unsigned char * elements;

	int remaining(void);
};

