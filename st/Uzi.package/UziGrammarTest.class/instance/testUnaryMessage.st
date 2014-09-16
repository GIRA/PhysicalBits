grammar-messages
testUnaryMessage
	| pp msg |
	pp := UziGrammar new unaryMessage.
	msg := pp parse: 'foo'.
	self assert: msg size = 2.
	self assert: msg first size = 1.
	self assert: msg first first inputValue = 'foo'.
	self assert: msg second isEmpty