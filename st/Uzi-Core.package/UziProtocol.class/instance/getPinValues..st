accessing-pins
getPinValues: pinNumber
	pinLastAccess at: pinNumber put: Time millisecondClockValue.
	^ self privateGetPinValues: pinNumber