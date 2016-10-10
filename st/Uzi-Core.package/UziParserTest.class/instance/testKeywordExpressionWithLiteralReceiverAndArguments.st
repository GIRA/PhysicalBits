grammar-messages
testKeywordExpressionWithLiteralReceiverAndArguments
	| pp send |
	pp := UziParser new keywordExpression.
	send := pp parse: '42 foo: 24 bar: -42'.
	self assert: send receiver value = 42.
	self assert: send selector = #foo:bar:.
	self assert: send arguments size = 2.
	self assert: send arguments first value = 24.
	self assert: send arguments second value = -42.