tests
test027Assignment
	| ast pp |
	pp := parser assignment end.
	ast := pp parse: 'variable = 42;'.
	self assert: #variable equals: ast left name.
	self assert: 42 equals: ast right value