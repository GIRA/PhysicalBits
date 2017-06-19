tests
test026VariableDeclarationWithExplicitValue
	| ast pp |
	pp := parser variableDeclaration end.
	ast := pp parse: 'var a = 5;'.
	self assert: #a equals: ast name.
	self assert: 5 equals: ast value value