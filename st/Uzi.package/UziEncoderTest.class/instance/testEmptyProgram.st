as yet unclassified
testEmptyProgram
	| program encoded |
	program := UziProgram
		inputs: #(10)
		outputs: #()
		scripts: {}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size = 7.
	self assert: encoded equals: #[0 0 2r00100000 0 0 0 0]