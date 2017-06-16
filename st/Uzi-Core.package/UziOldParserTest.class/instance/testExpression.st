tests
testExpression
	| pp expr expr1 |
	pp := parser expression.
	expr := pp parse: '1 foo bar'.
	self assert: expr selector = #bar.
	self assert: 1 equals: expr arguments size.
	self assert: expr arguments first notNil.
	expr1 := expr arguments first.
	self assert: expr1 selector = #foo.
	self assert: 1 equals: expr1 arguments size.
	self assert: 1 equals: expr1 arguments first value