#pragma once

#include "ReadStream.h"

class ArrayStream : public ReadStream {

public:
	ArrayStream(unsigned char*, int);
	~ArrayStream(void) {}
	
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

