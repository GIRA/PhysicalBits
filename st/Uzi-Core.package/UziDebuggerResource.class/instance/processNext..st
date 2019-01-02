actions
processNext: req
	[self debugger stepNext]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req send200Response: 'OK'