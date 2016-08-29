#pragma once

#include "Reader.h"
#include "Arduino.h"

class SerialReader : public Reader
{

public:
	SerialReader() {}
	~SerialReader(void) {}

	uint8 next(bool&);
};

