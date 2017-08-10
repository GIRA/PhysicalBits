tests
test040BlockWithStatements
	| ast pp |
	pp := parser block end.
	ast := pp parse: '{
		1; D13; toggle(D13);
		a = 0;
		if1{}else{}
		do{}while1;
		while1{}
		return 1;
	}'.
	self assert: 8 equals: ast statements size