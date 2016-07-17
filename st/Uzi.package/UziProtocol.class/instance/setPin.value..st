accessing-pins
setPin: pinNumber value: pinValue 
	pinValues at: pinNumber put: pinValue.
	^ self send: {
		1. 
		pinNumber. 
		(pinValue * 255) rounded
	}