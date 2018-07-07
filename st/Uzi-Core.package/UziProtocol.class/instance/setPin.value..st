accessing-pins
setPin: pinNumber value: pinValue 
	pinValues at: pinNumber put: pinValue.
	^ self send: {
		self class msgOutSetValue. 
		pinNumber. 
		(pinValue * 255) rounded
	}