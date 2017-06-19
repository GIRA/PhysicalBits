tests
test028Return
	| ast pp |
	pp := parser return end.
	ast := pp parse: 'return 42;'.
	self assert: 42 equals: ast value value