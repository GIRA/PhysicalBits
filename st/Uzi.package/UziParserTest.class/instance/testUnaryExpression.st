grammar-messages
testUnaryExpression
	| pp send |
	pp := UziParser new unaryExpression.
	send := pp parse: 'foo'.
	self assert: send receiver = nil.
	self assert: send selector = #foo.
	self assert: send arguments = #()