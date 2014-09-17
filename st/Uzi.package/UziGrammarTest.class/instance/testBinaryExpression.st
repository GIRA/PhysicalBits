grammar-messages
testBinaryExpression
	| pp msg |
	pp := UziGrammar new binaryExpression.
	msg := pp parse: '+ foo'.
	self assert: (self 
		isBinaryExpression: msg 
		receiver: [:rcvr |
			self isUnaryExpression: rcvr
				receiver: nil
				messages: #()]
		selector: '+'
		argument: [:arg | self 
			isUnaryExpression: arg
			receiver: nil 
			messages: #('foo')]).