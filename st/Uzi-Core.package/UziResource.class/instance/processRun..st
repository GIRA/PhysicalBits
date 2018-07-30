requests
processRun: req
	| src program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	[program := protocol compileString: src.
	protocol run: program]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	self sendJSONResponse: (Uzi encode: program) asArray 
		for: req