grammar-messages
testKeywordExpressionWithLiteralReceiver
	| pp send send2 |
	pp := UziParser new keywordExpression.
	send := pp parse: '42 bar: foo'.
	self assert: send receiver value = 42.
	self assert: send selector = #bar:.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: send2 receiver = nil.
	self assert: send2 selector = #foo.
	self assert: send2 arguments = #()