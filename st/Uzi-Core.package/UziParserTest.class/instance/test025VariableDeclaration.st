tests
test025VariableDeclaration
	| ast pp |
	pp := parser variableDeclaration end.
	ast := pp parse: 'var a;'.
	self assert: #a equals: ast name.
	self assert: 0 equals: ast value value