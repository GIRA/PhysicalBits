tests
test044ReturnWithNoValue
	| ast pp |
	pp := parser return end.
	ast := pp parse: 'return;'.
	self assert: nil equals: ast value