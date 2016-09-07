grammar-messages
testUnaryExpression
	| pp send |
	pp := UziParser new unaryExpression.
	send := pp parse: '1 foo'.
	self assert: 1 equals: send receiver value.
	self assert: send selector = #foo.
	self assert: send arguments = #()