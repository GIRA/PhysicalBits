grammar
testExpression3
	| pp expr rcvr arg1 arg2 |
	pp := UziParser new expression.
	expr := pp parse: 'foo bar: 42 + 24 qux: -42'.
	self assert: expr receiver notNil.
	self assert: expr selector = #bar:qux:.
	self assert: expr arguments size = 2.
	rcvr := expr receiver.
	self assert: rcvr receiver isNil.
	self assert: rcvr selector = #foo.
	self assert: rcvr arguments isEmpty.
	arg1 := expr arguments first.
	self assert: arg1 receiver value = 42.
	self assert: arg1 selector = #+.
	self assert: arg1 arguments size = 1.
	self assert: arg1 arguments first value = 24.
	arg2 := expr arguments second.
	self assert: arg2 value = -42