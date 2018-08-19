accessing
summary: req
	| res |
	res := super summary: req.
	self protocol currentProgram
		ifNotNil: [:program |
			res at: #src put: program sourceCode].
	^ res