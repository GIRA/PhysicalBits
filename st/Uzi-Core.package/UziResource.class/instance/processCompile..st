requests
processCompile: req
	| src type silent program |
	src := req fields at: 'src' ifAbsent: [^ req send400Response: 'Missing parameter: "src"'].
	type := req fields at: 'type' ifAbsent: ['text'].
	silent := (req fields at: 'silent' ifAbsent: [false]) = 'true'.
	[program := self compile: src type: type silent: silent]
		on: Error
		do: [:err | ^ req send400Response: err description].
	self send200ResponseFor: req