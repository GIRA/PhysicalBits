#pragma once

#include "ReadStream.h"
#include "HardwareSerial.h"

class SerialStream : public ReadStream {

public:
	SerialStream(HardwareSerial *);
	~SerialStream(void);
		
	unsigned char nextChar(void);
	
	unsigned char * upTo(unsigned char, bool);

private:

	HardwareSerial * _serial;
};

