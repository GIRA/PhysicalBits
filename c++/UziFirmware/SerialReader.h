#pragma once

#include "Reader.h"
#include "Arduino.h"

class SerialReader : public Reader
{

public:
	SerialReader() {}
	~SerialReader(void) {}

	unsigned char nextChar(void);
};

