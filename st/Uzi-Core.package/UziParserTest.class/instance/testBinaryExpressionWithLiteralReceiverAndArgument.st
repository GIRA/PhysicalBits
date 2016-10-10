grammar-messages
testBinaryExpressionWithLiteralReceiverAndArgument
	| pp send |
	pp := UziParser new binaryExpression.
	send := pp parse: '42 + -42'.
	self assert: send receiver value = 42.
	self assert: send selector = #+.
	self assert: send arguments size = 1.
	self assert: send arguments first value = -42