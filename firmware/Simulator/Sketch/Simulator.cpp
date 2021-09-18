#include "Simulator.h"

unsigned char __pinModes__[__TOTAL_PINS__];
unsigned short __pinValues__[__TOTAL_PINS__];

RuntimeStats Stats;

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


EXTERN unsigned short GPIO_getPinValue(unsigned int pin)
{
	return __getPinValue(pin);
}

EXTERN void GPIO_setPinValue(unsigned int pin, unsigned short value)
{
	__setPinValue(pin, value);
}

EXTERN void Sketch_setup(void)
{
	setup();
}

EXTERN void Sketch_loop(void)
{
	loop();
}

EXTERN size_t Serial_readInto(char* buffer, size_t len)
{
	return Serial.out_readInto(buffer, len);
}

EXTERN void Serial_write(char* str, size_t len)
{
	for (size_t i = 0; i < len; i++)
	{
		Serial.in_write(str[i]);
	}
}

EXTERN long Sketch_getMillis(void)
{
	return millis();
}

EXTERN void Sketch_setMillis(long millis)
{
	setMillis(millis);
}

EXTERN unsigned char EEPROM_read(int address)
{
	return EEPROM.read(address);
}

EXTERN void EEPROM_write(int address, unsigned char value)
{
	EEPROM.write(address, value);
}

EXTERN int EEPROM_size(void)
{
	return E2END + 1;
}

EXTERN uint16_t Stats_usedMemory() 
{
	return Stats.usedMemory;
}

EXTERN uint32_t Stats_coroutineResizeCounter()
{
	return Stats.coroutineResizeCounter;
}