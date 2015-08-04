as yet unclassified
testEmptyProgram
	| program encoded |
	program := UziProgram
		scripts: {}.
	encoded := UziEncoder new encode: program.
	self assert: encoded size = 1.
	self assert: encoded equals: #[0]