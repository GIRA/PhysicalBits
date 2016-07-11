#include "Simulator.h"

#define EXTERN extern "C" __declspec(dllexport)

unsigned char __pinModes__[__TOTAL_PINS__];
unsigned short __pinValues__[__TOTAL_PINS__];

unsigned short __getPinValue(unsigned int pin)
{
	int index = pin;
	if (index < 0 || index >= __TOTAL_PINS__)
	{
		return 0;
	}
	return __pinValues__[index];
}

void __setPinValue(unsigned int pin, unsigned short value)
{
	int index = pin;
	if (index < 0 || index >= __TOTAL_PINS__)
	{
		return;
	}

	unsigned short actualValue = value;
	if (value > 1023) actualValue = 1023;
	else if (value < 0) actualValue = 0;

	__pinValues__[index] = actualValue;
}

EXTERN long foo(void)
{
	return 42;
}


EXTERN unsigned short getPinValue(unsigned int pin)
{
	return __getPinValue(pin);
}

EXTERN void setPinValue(unsigned int pin, unsigned short value)
{
	__setPinValue(pin, value);
}

EXTERN void start(void)
{
	setup();
	for (;;)
		loop();
}

EXTERN size_t serial_readInto(char* buffer, size_t len)
{
	return Serial.out_readInto(buffer, len);
}

EXTERN void serial_write(char* str, int len)
{
	for (int i = 0; i < len; i++)
	{
		Serial.out_write(str[i]);
	}
}