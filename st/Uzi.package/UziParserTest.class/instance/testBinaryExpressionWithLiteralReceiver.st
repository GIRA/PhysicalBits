grammar-messages
testBinaryExpressionWithLiteralReceiver
	| pp send send2 |
	pp := UziParser new binaryExpression.
	send := pp parse: '42 + 1 foo'.
	self assert: send receiver value = 42.
	self assert: send selector = #+.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: send2 receiver value = 1.
	self assert: send2 selector = #foo.
	self assert: send2 arguments = #()