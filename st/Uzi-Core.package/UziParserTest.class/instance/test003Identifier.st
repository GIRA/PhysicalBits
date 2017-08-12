tests
test003Identifier
	| pp |
	pp := parser identifier end.
	self assert: 'Richo' equals: (pp parse: 'Richo').
	self assert: 'r' equals: (pp parse: 'r').
	self assert: (pp parse: '23richo') isPetitFailure.
	self assert: 'a.b.c' equals: (pp parse: 'a.b.c')