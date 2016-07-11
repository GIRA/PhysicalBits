#include "SerialReader.h"

unsigned char SerialReader::nextChar(void)
{
	while (Serial.available() <= 0);
	return (unsigned char)Serial.read();
}