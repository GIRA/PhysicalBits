#pragma once

#include "Reader.h"
#include "VSPDE.h"

class SerialReader : public Reader {

public:
	SerialReader() {}
	~SerialReader(void) {}
		
	unsigned char nextChar(void);
};

