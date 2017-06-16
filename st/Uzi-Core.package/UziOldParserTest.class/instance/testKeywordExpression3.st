tests
testKeywordExpression3
	| pp send send2 send3 |
	pp := parser keywordSend.
	send := pp parse: '1 foo: 2 bar baz: 3 qux'.
	self assert: 1 equals: send arguments first value.
	self assert: send selector = #foo:baz:.
	self assert: send arguments size = 3.
	send2 := send arguments second.
	self assert: 2 equals: send2 arguments first value.
	self assert: send2 selector = #bar.
	self assert: send2 arguments size = 1.
	send3 := send arguments third.
	self assert: 3 equals: send3 arguments first value.
	self assert: send3 selector = #qux.
	self assert: send3 arguments size = 1