tests
testUnarySelector
	| pp |
	pp := parser unarySelector end.
	self assert: 'foo' equals: (pp parse: 'foo').
	self assert: (pp parse: 'foo:') isPetitFailure