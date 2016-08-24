#include "Servo.h"

uint8_t Servo::attach(int pin)
{
	isAttached = true;
	return pin + 1;
}

void Servo::detach()
{
	isAttached = false;
}

void Servo::write(int value) {}

bool Servo::attached()
{
	return isAttached;
}