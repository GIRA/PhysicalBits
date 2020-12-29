#include "SerialReader.h"

#define TIMEOUT		100

uint8 SerialReader::next(bool& timeout)
{
	int32 start = millis();
	timeout = false;
	while (serial->available() <= 0)
	{
		timeout = millis() - start > TIMEOUT;
		if (timeout) return 0;
	}
	counter++;
	return (uint8)serial->read();
}