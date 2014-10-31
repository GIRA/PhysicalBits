as yet unclassified
testEncodingEmptyProgram
	| program encoded |
	program := UziProgram
		inputs: #(10)
		scripts: {}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size = 4.
	self assert: encoded equals: #[0 0 2r01000000 0]