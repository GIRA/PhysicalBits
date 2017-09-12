tests
test010ScriptWithMultipleStatements
	| pp ast stmt1 stmt2 |
	pp := parser script end.
	ast := pp parse: 'task foo() stopped { 
		toggle(D13); 
		write(A1, read(D11));
	}'.
	self assert: #foo equals: ast name.
	self assert: #stopped equals: ast state.
	self assert: nil equals: ast tickingRate.
	self assert: 2 equals: ast body statements size.
	stmt1 := ast body statements first.
	self assert: #toggle equals: stmt1 selector.
	stmt2 := ast body statements second.
	self assert: #write equals: stmt2 selector.