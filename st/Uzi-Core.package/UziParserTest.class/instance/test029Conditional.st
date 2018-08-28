tests
test029Conditional
	| ast pp trueBranch |
	pp := parser conditional end.
	ast := pp parse: 'if 1 { turnOn(D13); }'.
	self assert: 1 equals: ast condition value.
	self assert: 1 equals: ast trueBranch statements size.
	trueBranch := ast trueBranch.
	self assert: #turnOn equals: trueBranch statements first selector.
	self assert: 13 equals: trueBranch statements first arguments first value value.