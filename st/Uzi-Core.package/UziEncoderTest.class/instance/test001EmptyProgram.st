tests
test001EmptyProgram
	| program encoded |
	program := UziProgram empty.
	encoded := UziEncoder new encode: program.
	self assert: 2 equals: encoded size.
	self assert: #[0 0] equals: encoded