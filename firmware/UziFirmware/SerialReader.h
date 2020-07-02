#pragma once

#include "Reader.h"
#include "Arduino.h"
#include "UziSerial.h"

class SerialReader : public Reader
{
public:
	void init(UziSerial* s) { serial = s; }
	uint8 next(bool&);

private:
	UziSerial* serial;
};

