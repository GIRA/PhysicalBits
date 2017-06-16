tests
testKeywordExpression2
	| pp send send2 |
	pp := parser keywordSend.
	send := pp parse: '1 bar: 2 foo'.
	self assert: 1 equals: send arguments first value.
	self assert: send selector = #bar:.
	self assert: send arguments size = 2.
	send2 := send arguments second.
	self assert: 2 equals: send2 arguments first value.
	self assert: send2 selector = #foo.
	self assert: send2 arguments size = 1