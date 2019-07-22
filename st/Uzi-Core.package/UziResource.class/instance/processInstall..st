requests
processInstall: req
	| src type program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	type := req fields at: 'type' ifAbsent: ['text'].
	[program := self compile: src type: type.
	self controller install: program]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self send200ResponseFor: req