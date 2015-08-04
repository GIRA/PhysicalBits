as yet unclassified
testProgramHeader
	| program encoded |
	program := UziProgram
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size > 4.
	self assert: (encoded first: 4) 
		equals: #[2 0 2r00100011 0 ]