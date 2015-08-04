as yet unclassified
testEmptyProgram
	| program encoded |
	program := UziProgram
		scripts: {}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size = 7.
	self assert: encoded equals: #[0 0 2r00100000 0 0 0 0]