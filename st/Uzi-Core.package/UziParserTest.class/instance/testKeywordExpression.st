tests
testKeywordExpression
	| pp send send2 send3 |
	pp := parser keywordSend.
	send := pp parse: '1 bar: 2 + 3 foo'.
	self assert: 1 equals: send receiver value.
	self assert: send selector = #bar:.
	self assert: send arguments size = 1.
	send2 := send arguments first.
	self assert: 2 equals: send2 receiver value.
	self assert: send2 selector = #+.
	self assert: send2 arguments size = 1.
	send3 := send2 arguments first.
	self assert: 3 equals: send3 receiver value.
	self assert: send3 selector = #foo.
	self assert: send3 arguments = #()