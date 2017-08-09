tests
test043Procedure
	| ast pp |
	pp := parser script end.
	ast := pp parse: 'proc foo() { toggle(D13); }'.
	self assert: #foo equals: ast name