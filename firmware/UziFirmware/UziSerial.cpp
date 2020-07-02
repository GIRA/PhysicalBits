#include "UziSerial.h"

void UziSerial::begin() 
{
	Serial.begin(baudRate);
}

int UziSerial::available() 
{
	return Serial.available();
}

char UziSerial::read() 
{
	return Serial.read();
}

void UziSerial::write(unsigned char value) 
{
	Serial.write(value);
}
