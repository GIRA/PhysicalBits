reporting
setGlobal: name report: report
	| index |
	^ currentProgram ifNotNil: [
		index := currentProgram variables
			indexOf: (currentProgram variables detect: [:var | var name = name]).
		self send: {
			11. 
			index - 1. 
			report ifTrue: [1] ifFalse: [0]
		}]