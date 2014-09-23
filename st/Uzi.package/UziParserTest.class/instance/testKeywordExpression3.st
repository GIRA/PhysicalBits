grammar-messages
testKeywordExpression3
	| pp send send2 send3 |
	pp := UziParser new keywordExpression.
	send := pp parse: 'foo: bar baz: qux'.
	self assert: send receiver = nil.
	self assert: send selector = #foo:baz:.
	self assert: send arguments size = 2.
	send2 := send arguments first.
	self assert: send2 receiver = nil.
	self assert: send2 selector = #bar.
	self assert: send2 arguments = #().
	send3 := send arguments second.
	self assert: send3 receiver = nil.
	self assert: send3 selector = #qux.
	self assert: send3 arguments = #()