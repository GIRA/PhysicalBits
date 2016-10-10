grammar-messages
testKeywordExpression2
	| pp send send2 |
	pp := UziParser new keywordExpression.
	send := pp parse: '1 bar: 2 foo'.
	self assert: 1 equals: send receiver value.
	self assert: send selector = #bar:.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: 2 equals: send2 receiver value.
	self assert: send2 selector = #foo.
	self assert: send2 arguments = #()