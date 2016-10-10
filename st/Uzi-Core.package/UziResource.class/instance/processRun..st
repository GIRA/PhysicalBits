requests
processRun: req
	| src program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	[program := compiler compile: src.
	protocol run: program]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	req sendJSONResponse: (encoder encode: program) asArray 