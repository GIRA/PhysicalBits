requests
processInstall: req
	| src program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	[program := Uzi compileString: src.
	protocol run: program;	install: program]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	req sendJSONResponse: (Uzi encode: program) asArray 