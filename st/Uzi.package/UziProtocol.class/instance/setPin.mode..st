accessing-pins
setPin: pinNumber mode: pinMode 
	pinModes at: pinNumber put: pinMode.
	self send: {2. pinNumber. pinMode}