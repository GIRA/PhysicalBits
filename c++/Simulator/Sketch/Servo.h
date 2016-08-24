#pragma once


typedef unsigned char uint8_t;
typedef signed char int8_t;

class Servo
{
public:
	Servo() {}

	// attach the given pin to the next free channel, sets pinMode, returns channel number or 0 if failure
	uint8_t attach(int pin);
	
	void detach();

	// if value is < 200 its treated as an angle, otherwise as pulse width in microseconds 
	void write(int value);             

	// return true if this servo is attached, otherwise false 
	bool attached();                   
private:

	bool isAttached = false;
};

