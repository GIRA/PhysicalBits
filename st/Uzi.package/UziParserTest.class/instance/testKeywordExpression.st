grammar-messages
testKeywordExpression
	| pp send send2 send3 |
	pp := UziParser new keywordExpression.
	send := pp parse: 'bar: + foo'.
	self assert: send receiver = nil.
	self assert: send selector = #bar:.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: send2 receiver = nil.
	self assert: send2 selector = #+.
	self assert: send2 arguments size = 1.
	send3 := send2 arguments first.
	self assert: send3 receiver = nil.
	self assert: send3 selector = #foo.
	self assert: send3 arguments = #()