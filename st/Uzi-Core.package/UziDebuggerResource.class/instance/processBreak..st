actions
processBreak: req 
	[self debugger break]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req send200Response: 'OK'