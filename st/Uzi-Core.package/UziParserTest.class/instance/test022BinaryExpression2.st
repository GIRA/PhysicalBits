tests
test022BinaryExpression2
	| pp ast ast2 |
	pp := parser expression end.
	ast := pp parse: '3 + 4 * 6'.
	self assert: #* equals: ast selector.
	self assert: 2 equals: ast arguments size.
	self assert: 6 equals: ast arguments second value.
	ast2 := ast arguments first.
	self assert: 3 equals: ast2 arguments first value.
	self assert: 4 equals: ast2 arguments second value.