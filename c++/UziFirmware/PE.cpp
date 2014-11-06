#include "PE.h"
#include "Arduino.h"

PE::PE(void) {
	for (int i = 0; i < TOTAL_PINS; i++) {
		_pinValues[i] = 0;
		_pinModes[i] = OUTPUT;
	}
	for (int j = 2; j < 20; j++) {
		setMode(j, OUTPUT);
	}
}

unsigned char PE::getMode(unsigned int pin) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return OUTPUT;
	} else {
		return _pinModes[index];
	}
}

void PE::setMode(unsigned int pin, unsigned char mode) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
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

long PE::getMillis() {
	return millis();
}

void PE::delayMs(unsigned long milliseconds) {
	delay(milliseconds);
}
