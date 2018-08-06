requests
processRun: req
	| src type program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	type := req fields at: 'type' ifAbsent: ['text'].
	[program := self compile: src type: type.
	self protocol run: program]
		on: Error
		do: [:err | ^ req send400Response: err description].	
	self sendJSONResponse: (Uzi encode: program) asArray 
		for: req