tests
testIdentifier
	| pp |
	pp := parser identifier.
	self assert: 'Richo' equals: (pp parse: 'Richo').
	self assert: 'r' equals: (pp parse: 'r').
	self assert: (pp parse: '23richo') isPetitFailure