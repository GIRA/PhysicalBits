primitives and tokens
testBinaryToken
	| pp |
	pp := UziGrammar new binaryToken.
	self assert: (pp parse: '+') inputValue = '+'.
	self assert: (pp parse: '<=>') inputValue = '<=>'.
	self assert: (pp parse: '[') isPetitFailure.
	self assert: (pp parse: ']') isPetitFailure.
	self assert: (pp parse: '|') isPetitFailure.
	self assert: (pp parse: '(') isPetitFailure.
	self assert: (pp parse: ')') isPetitFailure.
	self assert: (pp parse: '.') isPetitFailure.
	self assert: (pp parse: ':') isPetitFailure.
	self assert: (pp parse: '"') isPetitFailure.
	self assert: (pp parse: '''') isPetitFailure.
	self assert: (pp parse: '{') isPetitFailure.
	self assert: (pp parse: '}') isPetitFailure.