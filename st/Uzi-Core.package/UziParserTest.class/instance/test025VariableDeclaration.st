tests
test025VariableDeclaration
	| ast pp |
	pp := parser variableDeclaration end.
	ast := pp parse: 'var a;'.
	self assert: #a equals: ast name.
	self assert: nil equals: ast value value