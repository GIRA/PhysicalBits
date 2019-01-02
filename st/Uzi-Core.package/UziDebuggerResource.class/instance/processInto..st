actions
processInto: req
	[self debugger stepInto]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req send200Response: 'OK'