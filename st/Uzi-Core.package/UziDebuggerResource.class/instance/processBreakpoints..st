actions
processBreakpoints: req
	| breakpoints |
	breakpoints := ((req fields at: 'breakpoints' ifAbsent: ['']) 
		subStrings: ',')
		collect: [:each | each asInteger].
	[self debugger setUserBreakpoints: breakpoints; sendBreakpoints]
		on: Error
		do: [:err | ^ req send400Response: err description].
	req send200Response: 'OK'