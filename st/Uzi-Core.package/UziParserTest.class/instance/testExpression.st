grammar
testExpression
	| pp expr expr1 |
	pp := UziParser new expression.
	expr := pp parse: '1 foo bar'.
	self assert: expr receiver notNil.
	self assert: expr selector = #bar.
	self assert: expr arguments isEmpty.
	expr1 := expr receiver.
	self assert: 1 equals: expr1 receiver value.
	self assert: expr1 selector = #foo.
	self assert: expr1 arguments isEmpty