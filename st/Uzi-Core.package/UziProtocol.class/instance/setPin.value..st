accessing-pins
setPin: pinNumber value: pinValue 
	^ self send: {
		self class msgOutSetValue. 
		pinNumber. 
		(pinValue * 255) rounded
	}