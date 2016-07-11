#pragma once

#include "Arduino.h"

// The following macros will work for standard arduino, other versions should redefine.
#define TOTAL_PINS														   18
#define ARRAY_INDEX(pin)										  ((pin) - 2)
#define PIN_NUMBER(index)										((index) + 2)
#define IS_ANALOG(pin)							 ((pin) >= 14 && (pin) <= 19)
#define IS_DIGITAL(pin)										(!IS_ANALOG(pin))

class GPIO
{

public:
	GPIO(void)
	{
		for (int i = 0; i < TOTAL_PINS; i++)
		{
			_pinValues[i] = 0;
			_pinModes[i] = INPUT;
			_pinReport[i] = false;
		}
	}
	~GPIO(void) {}

	unsigned char getMode(unsigned int);
	void setMode(unsigned int, unsigned char);
	float getValue(unsigned int);
	void setValue(unsigned int, float);
	bool getReport(unsigned int);
	void setReport(unsigned int, bool);
	long getMillis(void);
	void delayMs(unsigned long);
	void reset(void);

private:

	float _pinValues[TOTAL_PINS];
	unsigned char _pinModes[TOTAL_PINS];
	bool _pinReport[TOTAL_PINS];

};


