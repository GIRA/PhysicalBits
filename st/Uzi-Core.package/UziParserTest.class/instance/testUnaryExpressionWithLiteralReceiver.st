grammar-messages
testUnaryExpressionWithLiteralReceiver
	| pp send |
	pp := UziParser new unaryExpression.
	send := pp parse: '42 foo'.
	self assert: send receiver value = 42.
	self assert: send selector = #foo.
	self assert: send arguments = #()