as yet unclassified
testEncodingProgramHeader2
	| program encoded |
	program := UziProgram
		inputs: #(1 4 10 15 21 24 31)
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9.
			Uzi exampleBlinkLed9.
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size > 5.
	self assert: (encoded first: 5) 
		equals: #[3 
			2r10010000
			2r01000010
			2r00001001
			2r00000010]