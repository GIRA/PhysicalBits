tests
testKeywordExpressionWithLiteralReceiver
	| pp send send2 |
	pp := parser keywordSend.
	send := pp parse: '42 bar: 1foo'.
	self assert: send receiver value = 42.
	self assert: send selector = #bar:.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: 1 equals: send2 receiver value.
	self assert: send2 selector = #foo.
	self assert: send2 arguments = #()