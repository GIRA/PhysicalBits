tests
test009ScriptWithOneStatement
	| pp ast stmt |
	pp := parser script end.
	ast := pp parse: 'task foo() { toggle(D13); }'.
	self assert: #foo equals: ast name.
	self assert: nil equals: ast state.
	self assert: nil equals: ast tickingRate.
	self assert: 1 equals: ast body statements size.
	stmt := ast body statements first.
	self assert: #toggle equals: stmt selector