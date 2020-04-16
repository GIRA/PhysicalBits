accessing
summary: req
	| res |
	res := super summary: req.
	res at: #current 
		put: (self controller currentProgram
			ifNotNil: [:program | Dictionary new
				at: #id put: program id;
				at: #src put: program sourceCode;
				at: #ast put: (UziJSON createJSONObject: program node);
				at: #compiled put: (UziJSON createJSONObject: program);
				yourself]).
	res at: #running 
		put: (self controller runningProgram
			ifNotNil: [:program | Dictionary new
				at: #id put: program id;
				at: #src put: program sourceCode;
				at: #ast put: (UziJSON createJSONObject: program node);
				at: #compiled put: (UziJSON createJSONObject: program);
				at: #bytecodes put: (Uzi encode: program) asArray;
				at: #validBreakpoints put: (program linesOfCode collect: [:each | each value]);
				yourself]).
	^ res