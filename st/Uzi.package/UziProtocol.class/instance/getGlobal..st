accessing-globals
getGlobal: name
	| index |
	index := currentProgram variables
		indexOf: (currentProgram variables detect: [:var | var name = name]).
	^ globals at: index - 1 ifAbsent: nil