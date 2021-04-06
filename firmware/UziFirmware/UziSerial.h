#pragma once
#include "Arduino.h"

class UziSerial
{
public:
	UziSerial() {}
	UziSerial(long baudRate) 
	{ 
		this->baudRate = baudRate; 
	}

	void begin();
	int available();
	char read();
	void write(unsigned char);

private:
	long baudRate = 9600;
};

