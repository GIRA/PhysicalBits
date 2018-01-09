accessing-globals
getGlobal: name
	| index |
	^ currentProgram ifNotNil: [| sortedGlobals |
		sortedGlobals := currentProgram allGlobals.
		index := sortedGlobals indexOf: (sortedGlobals detect: [:var | var name = name] ifNone: [nil]).
		index ifNotNil: [globals at: index - 1 ifAbsent: nil]]