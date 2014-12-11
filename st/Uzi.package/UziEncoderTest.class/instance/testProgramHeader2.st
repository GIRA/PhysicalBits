as yet unclassified
testProgramHeader2
	| program encoded |
	program := UziProgram
		inputs: #(0 1 4 10 15 21 23)
		outputs: #()
		scripts: {
			Uzi exampleButton10Led9.
			Uzi examplePot15Led9.
			Uzi exampleBlinkLed9.
		}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size > 4.
	self assert: (encoded first: 4) 
		equals: #[3 
			2r11001000
			2r00100001
			2r00000101]