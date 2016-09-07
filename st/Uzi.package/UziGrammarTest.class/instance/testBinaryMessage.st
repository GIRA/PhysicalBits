grammar-messages
testBinaryMessage
	| pp msg msg2 |
	pp := UziGrammar new binaryMessage.
	msg := pp parse: '+ 2 foo'.
	self assert: (self 
		isBinaryMessage: msg 
		selector: '+'
		argument: [:arg | self 
			isUnaryExpression: arg
			receiver: '2'
			messages: #('foo')]).
	msg2 := pp parse: ' ++ 2 foo  bar'.
	self assert: (self 
		isBinaryMessage: msg2
		selector: '++'
		argument: [:arg | self 
			isUnaryExpression: arg
			receiver: '2' 
			messages: #('foo' 'bar')]).