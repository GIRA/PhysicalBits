tests
testExpression3
	| pp expr rcvr arg1 arg2 |
	pp := parser expression.
	expr := pp parse: 'foo bar: 42 + 24 qux: -42'.
	self assert: expr arguments first notNil.
	self assert: expr selector = #bar:qux:.
	self assert: expr arguments size = 3.
	rcvr := expr arguments first.
	self assert: 'foo' equals: rcvr name.
	arg1 := expr arguments second.
	self assert: arg1 arguments first value = 42.
	self assert: arg1 selector = #+.
	self assert: arg1 arguments size = 2.
	self assert: arg1 arguments second value = 24.
	arg2 := expr arguments third.
	self assert: arg2 value = -42