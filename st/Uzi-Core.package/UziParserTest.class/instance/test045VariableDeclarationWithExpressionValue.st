tests
test045VariableDeclarationWithExpressionValue
	| ast pp |
	pp := parser variableDeclaration end.
	ast := pp parse: 'var a = toggle(a);'.
	self assert: #a equals: ast name.
	self assert: #toggle equals: ast value selector.
	self assert: #a equals: ast value arguments first name