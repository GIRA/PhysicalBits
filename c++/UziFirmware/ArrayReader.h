#pragma once

#include "Reader.h"

class ArrayReader : public Reader
{

public:
	ArrayReader(unsigned char*, int);
	~ArrayReader(void) {}

	bool isClosed(void);

	unsigned char nextChar(void);

	int position(void);

	unsigned char * upTo(unsigned char, bool);

private:

	int _position;
	int _size;
	unsigned char * _elements;

	int size(void);
	int remaining(void);
};

