primitives and tokens
testUnaryToken
	| pp |
	pp := UziGrammar new unaryToken.
	self assert: (pp parse: 'foo') inputValue = 'foo'.
	self assert: (pp parse: 'foo:') isPetitFailure