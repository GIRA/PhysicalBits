accessing-pins
setPin: pinNumber mode: pinMode 
	pinModes at: pinNumber put: pinMode.
	^ self send: { self class msgOutSetMode . pinNumber . pinMode }