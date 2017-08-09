tests
test040EmptyBlock
	| ast pp |
	pp := parser block end.
	ast := pp parse: '{}'.
	self assert: 0 equals: ast statements size