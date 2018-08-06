accessing-pins
setPin: pinNumber value: pinValue 
	(self getPinValues: pinNumber) 
		add: DateAndTime now -> pinValue.
	^ self send: {
		self class msgOutSetValue. 
		pinNumber. 
		(pinValue * 255) rounded
	}