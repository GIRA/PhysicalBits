#pragma once

#include "Arduino.h"
#include <Servo.h>
#include "types.h"

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
		for (uint8 i = 0; i < TOTAL_PINS; i++)
		{
			pinValues[i] = 0;
			pinModes[i] = INPUT;
			pinReport[i] = false;
		}
	}
	~GPIO(void) {}

	uint8 getMode(uint16);
	void setMode(uint16, uint8);
	float getValue(uint16);
	void setValue(uint16, float);
	void servoWrite(uint16, float);
	bool getReport(uint16);
	void setReport(uint16, bool);
	void reset(void);

private:

	float pinValues[TOTAL_PINS];
	uint8 pinModes[TOTAL_PINS];
	bool pinReport[TOTAL_PINS];

};


