#pragma once

#include "Reader.h"
#include "HardwareSerial.h"

class SerialReader : public Reader {

public:
	SerialReader(HardwareSerial *);
	~SerialReader(void);
		
	unsigned char nextChar(void);

private:

	HardwareSerial * _serial;
};

