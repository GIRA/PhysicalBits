accessing-globals
getGlobal: name
	| index |
	^ currentProgram ifNotNil: [
		index := currentProgram variables
			indexOf: (currentProgram variables detect: [:var | var name = name] ifNone: [nil]).
		index ifNotNil: [globals at: index - 1 ifAbsent: nil]]