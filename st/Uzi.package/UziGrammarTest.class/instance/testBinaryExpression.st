grammar-messages
testBinaryExpression
	| pp msg |
	pp := UziGrammar new binaryExpression.
	msg := pp parse: '1 + 2 foo'.
	self assert: (self 
		isBinaryExpression: msg 
		receiver: [:rcvr |
			self isUnaryExpression: rcvr
				receiver: '1'
				messages: #()]
		selector: '+'
		argument: [:arg | self 
			isUnaryExpression: arg
			receiver: '2' 
			messages: #('foo')]).