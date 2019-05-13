#pragma once
#include "Arduino.h"

class UziSerial
{
public:
	void begin(long);
	int available();
	char read();
	void write(unsigned char);
};

