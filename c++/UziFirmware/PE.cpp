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
	_pinModes[index] = mode;
    pinMode(pin, mode);
}

unsigned short PE::getValue(unsigned int pin) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return 0;
	}
	
    if (getMode(pin) == OUTPUT) {
		return _pinValues[index];
    } else {
		if (IS_ANALOG(pin)) {
			return analogRead(pin);
		} else {
			// digitalRead() returns either 0 or 1, we treat HIGH as 255 so we multiply.
			return digitalRead(pin) * 255;
		}
    }
}

void PE::setValue(unsigned int pin, unsigned short value) {
	int index = ARRAY_INDEX(pin);
	if (index < 0 || index >= TOTAL_PINS) {
		return;
	}

	// We want to keep the value between 0 and 255, which is what analogWrite() can handle.
	unsigned short actualValue;
	if (value < 0) {
		actualValue = 0;
	} else if (value > 255) {
		actualValue = 255;
	} else {
		actualValue = value;
	}
	
	_pinValues[index] = actualValue;
	if (getMode(pin) == OUTPUT) {
		// It seems counter-intuitive but analog pins don't suppor analogWrite(), this is
		// because analogWrite() sends a PWM signal and has nothing to do with analog pins
		// or analogRead().
		// So, we check in order to avoid sending analogWrite() to an analog pin. Also, if
		// the value is either 0 or 255 there is no need for analogWrite(), we should use 
		// digitalWrite().
		if (IS_ANALOG(pin) || actualValue == 255 || actualValue == 0) {
			digitalWrite(pin, actualValue);
		} else {
			analogWrite(pin, actualValue);
		}
	}
}

long PE::getMillis() {
	return millis();
}

void PE::delayMs(unsigned long milliseconds) {
	delay(milliseconds);
}
