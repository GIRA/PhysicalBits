#include "GPIO.h"

Servo servos[TOTAL_PINS];
uint8 tonePin = 0;

uint8 GPIO::getMode(uint8 pin)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS)
	{
		return INPUT;
	}
	else
	{
		return pinFlags[index].mode;
	}
}

void GPIO::setMode(uint8 pin, uint8 mode)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS)
	{
		return;
	}
	// Avoid changing the pin mode if it was already set
	if (pinFlags[index].mode == mode)
	{
		return;
	}
	pinFlags[index].mode = mode;
	pinMode(pin, mode);
}

float GPIO::getValue(uint8 pin)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS)
	{
		return 0;
	}

	if (getMode(pin) == OUTPUT)
	{
		return pinValues[index];
	}
	else
	{
		if (IS_ANALOG(pin))
		{
			// analogRead() returns a value between 0 and 1023, we treat HIGH as 1 so we divide.
			return (float)analogRead(pin) / 1023;
		}
		else
		{
			return digitalRead(pin);
		}
	}
}

void GPIO::setValue(uint8 pin, float value)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS)
	{
		return;
	}

	// We must keep the value between 0 and 1
	float actualValue;
	if (value <= 0)
	{
		actualValue = 0;
	}
	else if (value >= 1)
	{
		actualValue = 1;
	}
	else
	{
		actualValue = value;
	}

	pinValues[index] = actualValue;
	if (getMode(pin) != OUTPUT)
	{
		setMode(pin, OUTPUT);
	}
	if (servos[index].attached())
	{
		servos[index].detach();
	}
	// It seems counter-intuitive but analog pins don't support analogWrite(), this is
	// because analogWrite() sends a PWM signal and has nothing to do with analog pins
	// or analogRead().
	// So, we check in order to avoid sending analogWrite() to an analog pin. Also, if
	// the value is close to either 0 or 1 there is no need for analogWrite(), we should 
	// use digitalWrite().
	if (IS_ANALOG(pin) || actualValue > 0.996 || actualValue < 0.004)
	{
		digitalWrite(pin, actualValue > 0.5 ? HIGH : LOW);
	}
	else
	{
		analogWrite(pin, (uint16)round(actualValue * 255));
	}
}

void GPIO::servoWrite(uint8 pin, float value)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS)
	{
		return;
	}

	// We must keep the value between 0 and 1
	float actualValue;
	if (value <= 0)
	{
		actualValue = 0;
	}
	else if (value >= 1)
	{
		actualValue = 1;
	}
	else
	{
		actualValue = value;
	}
	pinValues[index] = actualValue;
	pinFlags[index].mode = OUTPUT;

	int16 degrees = (int16)round(actualValue * 180.0);
	if (!servos[index].attached())
	{
		servos[index].attach(pin);
	}
	servos[index].write(degrees);
}

void GPIO::startTone(uint8 pin, float freq) 
{
	if (tonePin != pin) 
	{
		/* 
		 * NOTE(Richo): Make sure we send noTone(..) before changing pins. Otherwise the tone won't play. 
		 * See the "Notes and warning" section on: https://www.arduino.cc/reference/en/language/functions/advanced-io/tone/
		 */
		if (tonePin > 0) { noTone(tonePin); }
		tonePin = pin;
	}
	setMode(pin, OUTPUT);
	tone(pin, (uint32)freq);
}

void GPIO::stopTone(uint8 pin)
{
	setMode(pin, OUTPUT);
	noTone(pin);
}

void GPIO::setReport(uint8 pin, bool report)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS) return;

	pinFlags[index].report = report;
}

bool GPIO::getReport(uint8 pin)
{
	uint8 index = ARRAY_INDEX(pin);
	if (index >= TOTAL_PINS) return false;

	return pinFlags[index].report;
}

void GPIO::reset()
{
	for (uint8 i = 0; i < TOTAL_PINS; i++)
	{
		if (pinFlags[i].mode == OUTPUT)
		{
			noTone(PIN_NUMBER(i));
			setValue(PIN_NUMBER(i), 0);
			setMode(PIN_NUMBER(i), INPUT);
		}
		pinValues[i] = 0;
		pinFlags[i].mode = INPUT;
	}
}