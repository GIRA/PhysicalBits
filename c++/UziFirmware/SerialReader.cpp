#include "SerialReader.h"

unsigned char SerialReader::next(void)
{
	while (Serial.available() <= 0);
	return (unsigned char)Serial.read();
}