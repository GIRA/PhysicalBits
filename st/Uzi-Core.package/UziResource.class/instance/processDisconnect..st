requests
processDisconnect: req
	[protocol disconnect]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self sendJSONResponse: self detail
		for: req