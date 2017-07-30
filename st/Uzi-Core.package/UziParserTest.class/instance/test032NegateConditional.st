tests
test032NegateConditional
	| ast pp trueBranch falseBranch condition |
	pp := parser conditional end.
	ast := pp parse: 'if (!read(D13)) { turnOff(D13); } else { turnOn (D13);}'.
	self assert: 1 equals: ast trueBranch statements size.
	self assert: 1 equals: ast falseBranch statements size.
	condition := ast condition.
	self assert: #not equals: condition selector.
	self assert: #read equals: condition arguments first selector.
	self assert: 13 equals: condition arguments first arguments first value.
	trueBranch := ast trueBranch.
	self assert: #turnOff equals: trueBranch statements first selector.
	self assert: 13 equals: trueBranch statements first arguments first value.
	falseBranch := ast falseBranch.
	self assert: #turnOn equals: falseBranch statements first selector.
	self assert: 13 equals: falseBranch statements first arguments first value.