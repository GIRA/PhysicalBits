tests
testBinaryExpressionWithLiteralReceiverAndArgument
	| pp send |
	pp := parser binarySend.
	send := pp parse: '42 + -42'.
	self assert: send selector = #+.
	self assert: send arguments size = 2.
	self assert: send arguments first value = 42.
	self assert: send arguments second value = -42