grammar-messages
testKeywordExpression
	| pp expr |
	pp := UziGrammar new keywordExpression.
	expr := pp parse: 'foo: bar'.
	self assert: (self 
		isKeywordExpression: expr 
		receiver: [:rcvr | self
			isUnaryExpression: rcvr
			receiver: nil
			messages: #()]
		selectors: #('foo:')
		arguments: [:args |
			args size = 1 and: [self
				isUnaryExpression: args first
				receiver: nil
				messages: #('bar')]]).