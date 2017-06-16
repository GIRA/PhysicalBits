tests
testExpression2
	| pp expr arg rcvr |
	pp := parser expression.
	expr := pp parse: '42 foo + 24 bar'.
	self assert: expr arguments first notNil.
	self assert: expr selector = #+.
	self assert: expr arguments size = 2.
	rcvr := expr arguments first.
	self assert: rcvr arguments first value = 42.
	self assert: rcvr selector = #foo.
	self assert: 1 equals: rcvr arguments size.
	arg := expr arguments second.
	self assert: arg arguments first value = 24.
	self assert: arg selector = #bar.
	self assert: 1 equals: arg arguments size