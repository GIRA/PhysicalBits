reporting
setGlobal: name report: report
	| index |
	^ currentProgram ifNotNil: [| sortedGlobals |
		sortedGlobals := currentProgram allGlobals.
		index := sortedGlobals indexOf: (sortedGlobals detect: [:var | var name = name]).
		globals at: index - 1 put: nil.
		report 
			ifTrue: [globalsReporting add: name]
			ifFalse: [globalsReporting remove: name ifAbsent: ["Do nothing"]].
		self send: {
			11. 
			index - 1. 
			report ifTrue: [1] ifFalse: [0]
		}]