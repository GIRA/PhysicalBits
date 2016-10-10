grammar-messages
testKeywordExpression
	| pp expr |
	pp := UziGrammar new keywordExpression.
	expr := pp parse: '1 foo: 2 + 3 bar'.
	self assert: (self 
		isKeywordExpression: expr 
		receiver: [:rcvr | self
			isUnaryExpression: rcvr
			receiver: '1'
			messages: #()]
		selectors: #('foo:')
		arguments: [:args |
			args size = 1 and: [self
				isBinaryExpression: args first
				receiver: [:rcvr | self
					isUnaryExpression: rcvr
					receiver: '2'
					messages: #()]
				selector: '+'
				argument: [:arg | self
					isUnaryExpression: arg
					receiver: '3'
					messages: #('bar')]]]).