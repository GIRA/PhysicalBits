#include "SerialStream.h"


SerialStream::SerialStream(HardwareSerial * serial) {
	_serial = serial;
}

unsigned char SerialStream::nextChar(void) {
	while(_serial->available() <= 0);
	return (unsigned char)_serial->read();
}