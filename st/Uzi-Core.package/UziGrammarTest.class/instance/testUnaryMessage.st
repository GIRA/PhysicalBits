grammar-messages
testUnaryMessage
	| pp msg |
	pp := UziGrammar new unaryMessage.
	msg := pp parse: 'foo'.
	self assert: (self isUnaryMessage: msg value: 'foo')