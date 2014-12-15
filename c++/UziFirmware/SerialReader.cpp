#include "SerialReader.h"


SerialReader::SerialReader(HardwareSerial * serial) {
	_serial = serial;
}

unsigned char SerialReader::nextChar(void) {
	while(_serial->available() <= 0);
	return (unsigned char)_serial->read();
}