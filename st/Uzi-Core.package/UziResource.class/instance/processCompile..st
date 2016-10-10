requests
processCompile: req
	| src program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	[program := compiler compile: src]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	req sendJSONResponse: (encoder encode: program) asArray