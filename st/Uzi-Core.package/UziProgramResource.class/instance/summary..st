accessing
summary: req
	| res |
	res := super summary: req.
	self protocol currentProgram
		ifNotNil: [:program |
			res at: #id put: program node id;
				at: #src put: program sourceCode;
				at: #ast put: (UziJSON createJSONObject: program node);
				at: #bytecodes put: (Uzi encode: program) asArray].
	^ res