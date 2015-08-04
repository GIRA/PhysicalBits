as yet unclassified
testProgramHeader2
	| program encoded |
	program := UziProgram
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9.
			Uzi exampleBlinkLed9.
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded first equals: 3