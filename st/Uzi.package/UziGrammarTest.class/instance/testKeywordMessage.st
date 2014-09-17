grammar-messages
testKeywordMessage
	| pp msg msg2 |
	pp := UziGrammar new keywordMessage.
	msg := pp parse: 'foo: + bar'.
	self assert: (self 
		isKeywordMessage: msg 
		selectors: #('foo:')
		arguments: [:args |
			args size = 1 and: [self
				isBinaryExpression: args first
				receiver: [:rcvr | self 
					isUnaryExpression: rcvr 
					receiver: nil 
					messages: #()]
				selector: '+'
				argument: [:arg | self 
					isUnaryExpression: arg
					receiver: nil
					messages: #('bar')]]]).
	msg2 := pp parse: 'foo: + bar bar: - baz'.
	self assert: (self 
		isKeywordMessage: msg2
		selectors: #('foo:' 'bar:')
		arguments: [:args |
			args size = 2 
				and: [(self
					isBinaryExpression: args first
					receiver: [:rcvr | self 
						isUnaryExpression: rcvr 
						receiver: nil 
						messages: #()]
					selector: '+'
					argument: [:arg | self 
						isUnaryExpression: arg
						receiver: nil
						messages: #('bar')])
					and: [self
						isBinaryExpression: args second
						receiver: [:rcvr | self 
							isUnaryExpression: rcvr 
							receiver: nil 
							messages: #()]
						selector: '-'
						argument: [:arg | self 
							isUnaryExpression: arg
							receiver: nil
							messages: #('baz')]]]]).