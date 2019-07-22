requests
processDisconnect: req
	[self controller disconnect]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self send200ResponseFor: req