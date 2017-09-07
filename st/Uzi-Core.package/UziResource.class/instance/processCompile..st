requests
processCompile: req
	| src program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	[program := Uzi compileString: src]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	self sendJSONResponse: (Uzi encode: program) asArray 
		for: req