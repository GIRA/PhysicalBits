tests
testKeywordExpressionWithLiteralReceiverAndArguments
	| pp send |
	pp := parser keywordSend.
	send := pp parse: '42 foo: 24 bar: -42'.
	self assert: send selector = #foo:bar:.
	self assert: send arguments size = 3.
	self assert: send arguments first value = 42.
	self assert: send arguments second value = 24.
	self assert: send arguments third value = -42.