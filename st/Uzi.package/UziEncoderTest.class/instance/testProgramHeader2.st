as yet unclassified
testProgramHeader2
	| program encoded |
	program := UziProgram
		inputs: #(1 4 10 15 21 23 24)
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9.
			Uzi exampleBlinkLed9.
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size > 4.
	self assert: (encoded first: 4) 
		equals: #[3 
			2r10010000
			2r01000010
			2r00001011]