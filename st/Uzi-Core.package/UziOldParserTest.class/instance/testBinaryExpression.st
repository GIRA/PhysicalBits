tests
testBinaryExpression
	| pp send send2 |
	pp := parser binarySend.
	send := pp parse: '1 + 2 foo'.
	self assert: send arguments first value = 1.
	self assert: send selector = #+.
	self assert: send arguments size = 2.
	send2 := send arguments second.
	self assert: send2 arguments first value = 2.
	self assert: send2 selector = #foo.
	self assert: send2 arguments size = 1