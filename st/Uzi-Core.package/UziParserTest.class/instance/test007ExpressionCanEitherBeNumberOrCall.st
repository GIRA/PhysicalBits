tests
test007ExpressionCanEitherBeNumberOrCall
	| pp |
	pp := parser expression end.
	self deny: (pp parse: '  read(D13)  ') isPetitFailure.
	self deny: (pp parse: '  0.4  ') isPetitFailure