grammar-messages
testKeywordMessage
	| pp msg msg2 |
	pp := UziGrammar new keywordMessage.
	msg := pp parse: 'foo: 1 + 2 bar'.
	self assert: (self 
		isKeywordMessage: msg 
		selectors: #('foo:')
		arguments: [:args |
			args size = 1 and: [self
				isBinaryExpression: args first
				receiver: [:rcvr | self 
					isUnaryExpression: rcvr 
					receiver: '1'
					messages: #()]
				selector: '+'
				argument: [:arg | self 
					isUnaryExpression: arg
					receiver: '2'
					messages: #('bar')]]]).
	msg2 := pp parse: 'foo: 1 + 2 bar bar: 3 - 4 baz'.
	self assert: (self 
		isKeywordMessage: msg2
		selectors: #('foo:' 'bar:')
		arguments: [:args |
			args size = 2 
				and: [(self
					isBinaryExpression: args first
					receiver: [:rcvr | self 
						isUnaryExpression: rcvr 
						receiver: '1'
						messages: #()]
					selector: '+'
					argument: [:arg | self 
						isUnaryExpression: arg
						receiver: '2'
						messages: #('bar')])
					and: [self
						isBinaryExpression: args second
						receiver: [:rcvr | self 
							isUnaryExpression: rcvr 
							receiver: '3'
							messages: #()]
						selector: '-'
						argument: [:arg | self 
							isUnaryExpression: arg
							receiver: '4'
							messages: #('baz')]]]]).