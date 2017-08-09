tests
test043Function
	| ast pp |
	pp := parser script end.
	ast := pp parse: 'func t() { return 1; }'.
	self assert: #t equals: ast name