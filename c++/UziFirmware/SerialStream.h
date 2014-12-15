#pragma once

#include "ReadStream.h"
#include "HardwareSerial.h"

class SerialStream : public ReadStream {

public:
	SerialStream(HardwareSerial *);
	~SerialStream(void);
		
	unsigned char nextChar(void);

private:

	HardwareSerial * _serial;
};

