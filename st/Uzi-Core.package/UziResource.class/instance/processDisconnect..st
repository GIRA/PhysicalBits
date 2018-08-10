requests
processDisconnect: req
	[self protocol disconnect]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self sendJSONResponse: (self detail: req)
		for: req