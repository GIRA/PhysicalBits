grammar-messages
testUnaryExpression
	| pp expr expr2 expr3 |
	pp := UziGrammar new unaryExpression.
	expr := pp parse: '1 foo'.
	self assert: (self isUnaryExpression: expr receiver: '1' messages: #('foo')).
	expr2 := pp parse: '1 foo bar'.
	self assert: (self isUnaryExpression: expr2 receiver: '1' messages: #('foo' 'bar')).
	expr3 := pp parse: '42 baz'.
	self deny: (self isUnaryExpression: expr3 receiver: nil messages: #('baz')).