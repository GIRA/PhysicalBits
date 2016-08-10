as yet unclassified
testProgramHeader
	| program encoded |
	program := UziProgram
		scripts: {
			UziScriptBuilder exampleButton10Led9.
			UziScriptBuilder examplePot15Led9
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded first equals: 2