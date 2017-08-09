tests
test041BlockWithVariableDeclarations
	| ast pp |
	pp := parser block end.
	ast := pp parse: '{
		var a = D13;
		toggle(a);
		a = 0;
		if a {} else {}
		do {} while a
		while a {}
		return a;
	}'.
	self assert: 1 equals: ast locals size