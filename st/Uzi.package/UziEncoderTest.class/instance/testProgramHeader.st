as yet unclassified
testProgramHeader
	| program encoded |
	program := UziProgram
		inputs: #(10 15 14)
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size > 4.
	self assert: (encoded first: 4) 
		equals: #[2 0 2r01000110 0 ]