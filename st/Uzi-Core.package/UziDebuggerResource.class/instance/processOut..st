actions
processOut: req
	[self debugger stepOut]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req send200Response: 'OK'