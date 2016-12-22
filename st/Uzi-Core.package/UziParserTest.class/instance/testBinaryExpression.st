tests
testBinaryExpression
	| pp send send2 |
	pp := parser binarySend.
	send := pp parse: '1 + 2 foo'.
	self assert: send receiver value = 1.
	self assert: send selector = #+.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: send2 receiver value = 2.
	self assert: send2 selector = #foo.
	self assert: send2 arguments = #()