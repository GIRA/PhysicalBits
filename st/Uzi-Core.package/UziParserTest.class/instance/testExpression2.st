tests
testExpression2
	| pp expr arg rcvr |
	pp := parser expression.
	expr := pp parse: '42 foo + 24 bar'.
	self assert: expr receiver notNil.
	self assert: expr selector = #+.
	self assert: expr arguments size = 1.
	rcvr := expr receiver.
	self assert: rcvr receiver value = 42.
	self assert: rcvr selector = #foo.
	self assert: rcvr arguments isEmpty.
	arg := expr arguments first.
	self assert: arg receiver value = 24.
	self assert: arg selector = #bar.
	self assert: arg arguments isEmpty