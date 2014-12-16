#include "PE.h"
#include "Arduino.h"

unsigned char PE::getMode(unsigned int pin) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return INPUT;
	} else {
		return _pinModes[index];
	}
}

void PE::setMode(unsigned int pin, unsigned char mode) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return;
	}
	// Avoid changing the pin mode if it was already set
	if (_pinModes[index] == mode) {
		return;
	}
	_pinModes[index] = mode;
	pinMode(pin, mode);
}

float PE::getValue(unsigned int pin) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return 0;
	}
	
	if (getMode(pin) == OUTPUT) {
		return _pinValues[index];
	} else {
		if (IS_ANALOG(pin)) {
			// analogRead() returns a value between 0 and 1023, we treat HIGH as 1 so we divide.
			return (float)analogRead(pin) / 1023;
		} else {
			return digitalRead(pin);
		}
	}
}

void PE::setValue(unsigned int pin, float value) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return;
	}

	// We must keep the value between 0 and 1
	float actualValue;
	if (value <= 0) {
		actualValue = 0;
	} else if (value >= 1) {
		actualValue = 1;
	} else {
		actualValue = value;
	}
	
	_pinValues[index] = actualValue;
	if (getMode(pin) == OUTPUT) {
		// It seems counter-intuitive but analog pins don't support analogWrite(), this is
		// because analogWrite() sends a PWM signal and has nothing to do with analog pins
		// or analogRead().
		// So, we check in order to avoid sending analogWrite() to an analog pin. Also, if
		// the value is close to either 0 or 1 there is no need for analogWrite(), we should 
		// use digitalWrite().
		if (IS_ANALOG(pin) || actualValue > 0.996 || actualValue < 0.004) {
			digitalWrite(pin, actualValue > 0.5 ? HIGH : LOW);
		} else {
			analogWrite(pin, actualValue * 255);
		}
	}
}

void PE::setReport(unsigned int pin, bool report) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return;
	}
	_pinReport[index] = report;
}

bool PE::getReport(unsigned int pin) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return false;
	}
	return _pinReport[index];
}

long PE::getMillis() {
	return millis();
}

void PE::delayMs(unsigned long milliseconds) {
	delay(milliseconds);
}
