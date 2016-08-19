#include "SerialReader.h"

#define TIMEOUT		1000

unsigned char SerialReader::next(bool& timeout)
{
	long start = millis();
	timeout = false;
	while (Serial.available() <= 0)
	{
		timeout = millis() - start > TIMEOUT;
		if (timeout) return 0;
	}
	return (unsigned char)Serial.read();
}