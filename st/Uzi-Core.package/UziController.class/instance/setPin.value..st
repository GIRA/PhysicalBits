accessing-pins
setPin: pinName value: pinValue
	| pinNumber |
	pinNumber := self getPinNumber: pinName.
	^ self send: {
		self class msgOutSetValue. 
		pinNumber. 
		(pinValue * 255) rounded
	}