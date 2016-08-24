#include "Servo.h"

uint8_t Servo::attach(int pin)
{
	pinNumber = pin;
	isAttached = true;
	return pin + 1;
}

void Servo::detach()
{
	pinNumber = 0;
	isAttached = false;
}

void Servo::write(int value) 
{
	float actualValue = (float)value / 180.0 * 255;
	analogWrite(pinNumber, (unsigned short)round(actualValue));
}

bool Servo::attached()
{
	return isAttached;
}