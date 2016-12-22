tests
testBinarySelector
	| pp |
	pp := parser binarySelector.
	self assert: '+' equals: (pp parse: '+').
	self assert: '<=>' equals: (pp parse: '<=>').
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