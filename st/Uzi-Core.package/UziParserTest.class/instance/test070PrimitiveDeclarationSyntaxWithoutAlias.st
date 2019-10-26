tests
test070PrimitiveDeclarationSyntaxWithoutAlias
	| ast prim |
	ast := parser parse: 'prim turnOn;'.
	self assert: 1 equals: ast primitives size.
	prim := ast primitives anyOne.
	self assert: #turnOn equals: prim name.
	self assert: #turnOn equals: prim alias.