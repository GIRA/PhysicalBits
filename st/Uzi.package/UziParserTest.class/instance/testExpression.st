grammar
testExpression
	| pp expr expr1 |
	pp := UziParser new expression.
	expr := pp parse: 'foo bar'.
	self assert: expr receiver notNil.
	self assert: expr selector = #bar.
	self assert: expr arguments isEmpty.
	expr1 := expr receiver.
	self assert: expr1 receiver isNil.
	self assert: expr1 selector = #foo.
	self assert: expr1 arguments isEmpty