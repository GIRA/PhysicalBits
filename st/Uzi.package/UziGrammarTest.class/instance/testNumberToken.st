primitives and tokens
testNumberToken
	| pp |
	pp := UziGrammar new numberToken.
	self assert: (pp parse: '10') inputValue = '10'.
	self assert: (pp parse: '-10') inputValue = '-10'.
	self assert: (pp parse: 'r10') isPetitFailure