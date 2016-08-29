#include "GPIO.h"

Servo servos[TOTAL_PINS];

uint8 GPIO::getMode(uint16 pin)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
	{
		return INPUT;
	}
	else
	{
		return pinModes[index];
	}
}

void GPIO::setMode(uint16 pin, uint8 mode)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
	{
		return;
	}
	// Avoid changing the pin mode if it was already set
	if (pinModes[index] == mode)
	{
		return;
	}
	pinModes[index] = mode;
	pinMode(pin, mode);
}

float GPIO::getValue(uint16 pin)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
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

void GPIO::setValue(uint16 pin, float value)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
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
	if (getMode(pin) == INPUT)
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

void GPIO::servoWrite(uint16 pin, float value)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
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

	int16 degrees = (int16)round(actualValue * 180.0);
	if (!servos[index].attached())
	{
		servos[index].attach(pin);
	}
	servos[index].write(degrees);
}

void GPIO::setReport(uint16 pin, bool report)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
	{
		return;
	}
	pinReport[index] = report;
}

bool GPIO::getReport(uint16 pin)
{
	int16 index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS)
	{
		return false;
	}
	return pinReport[index];
}

void GPIO::reset()
{
	for (int16 i = 0; i < TOTAL_PINS; i++)
	{
		if (pinModes[i] == OUTPUT)
		{
			setValue(PIN_NUMBER(i), 0);
			setMode(PIN_NUMBER(i), INPUT);
		}
		pinValues[i] = 0;
		pinModes[i] = INPUT;
		pinReport[i] = false;
	}
}