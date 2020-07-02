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

struct PinFlags 
{
	uint8 mode : 2;
	uint8 report : 1;
	uint8 type : 5; // TODO(Richo)
};

class GPIO
{
public:
	GPIO(void)
	{
		for (uint8 i = 0; i < TOTAL_PINS; i++)
		{
			pinFlags[i].mode = INPUT;
			pinFlags[i].report = false;
			pinFlags[i].type = 0;
			pinValues[i] = 0;
		}
	}
	~GPIO(void) {}

	uint8 getMode(uint8);
	void setMode(uint8, uint8);
	float getValue(uint8);
	void setValue(uint8, float);
	void servoWrite(uint8, float);
	void startTone(uint8, float);
	void stopTone(uint8);
	bool getReport(uint8);
	void setReport(uint8, bool);
	void reset(void);

private:

	float pinValues[TOTAL_PINS];
	PinFlags pinFlags[TOTAL_PINS];
};


