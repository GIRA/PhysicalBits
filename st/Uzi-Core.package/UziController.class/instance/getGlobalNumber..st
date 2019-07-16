accessing-globals
getGlobalNumber: globalName
	^ runningProgram  
		ifNotNil: [:program || sortedGlobals global |
			sortedGlobals := program allGlobals.
			global := sortedGlobals detect: [:var | var name = globalName] ifNone: [nil].
			global ifNotNil: [(sortedGlobals indexOf: global) - 1]]