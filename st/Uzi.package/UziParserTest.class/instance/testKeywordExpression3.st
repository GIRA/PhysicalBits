grammar-messages
testKeywordExpression3
	| pp send send2 send3 |
	pp := UziParser new keywordExpression.
	send := pp parse: '1 foo: 2 bar baz: 3 qux'.
	self assert: 1 equals: send receiver value.
	self assert: send selector = #foo:baz:.
	self assert: send arguments size = 2.
	send2 := send arguments first.
	self assert: 2 equals: send2 receiver value.
	self assert: send2 selector = #bar.
	self assert: send2 arguments = #().
	send3 := send arguments second.
	self assert: 3 equals: send3 receiver value.
	self assert: send3 selector = #qux.
	self assert: send3 arguments = #()