tests
test021BinaryExpression
	| pp ast |
	pp := parser binary end.
	ast := pp parse: '3 + 4'.
	self assert: #+ equals: ast selector.
	self assert: 2 equals: ast arguments size.
	self assert: 3 equals: ast arguments first value.
	self assert: 4 equals: ast arguments second value.