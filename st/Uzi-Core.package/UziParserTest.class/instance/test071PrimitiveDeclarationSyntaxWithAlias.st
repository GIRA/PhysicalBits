tests
test071PrimitiveDeclarationSyntaxWithAlias
	| ast prim |
	ast := parser parse: 'prim + : add;'.
	self assert: 1 equals: ast primitives size.
	prim := ast primitives anyOne.
	self assert: #add equals: prim name.
	self assert: #+ equals: prim alias.