accessing-pins
getPinValues: pinNumber
	pinLastAccess at: pinNumber put: Time millisecondClockValue.
	(pinsReporting includes: pinNumber)
		ifFalse: [self setPin: pinNumber report: true].
	^ self privateGetPinValues: pinNumber