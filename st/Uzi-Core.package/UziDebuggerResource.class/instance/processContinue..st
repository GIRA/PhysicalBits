actions
processContinue: req 
	[self debugger continue]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req send200Response: 'OK'