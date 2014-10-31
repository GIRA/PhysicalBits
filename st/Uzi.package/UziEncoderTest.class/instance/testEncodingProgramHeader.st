as yet unclassified
testEncodingProgramHeader
	| program encoded |
	program := UziProgram
		inputs: #(10 15 )
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size > 5.
	self assert: (encoded first: 5) 
		equals: #[2 0 2r01000010 0 0]