tests
testEmptyBlock2 
	| pp b |
	pp := parser block.
	b := (pp parse: '[]') value.
	self assert: b statements isEmpty.
	self assert: (pp parse: 'cualquier cosa []') isPetitFailure