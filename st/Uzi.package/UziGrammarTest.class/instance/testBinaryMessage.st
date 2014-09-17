grammar-messages
testBinaryMessage
	| pp msg msg2 |
	pp := UziGrammar new binaryMessage.
	msg := pp parse: '+ foo'.
	self assert: (self 
		isBinaryMessage: msg 
		selector: '+'
		argument: [:arg | self 
			isUnaryExpression: arg
			receiver: nil 
			messages: #('foo')]).
	msg2 := pp parse: ' ++ foo  bar'.
	self assert: (self 
		isBinaryMessage: msg2
		selector: '++'
		argument: [:arg | self 
			isUnaryExpression: arg
			receiver: nil 
			messages: #('foo' 'bar')]).