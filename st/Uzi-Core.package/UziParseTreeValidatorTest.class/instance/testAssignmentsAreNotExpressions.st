tests
testAssignmentsAreNotExpressions
	self assertInvalid: 'var a; proc foo() { write(D13, a = 5); }'.
	self assertInvalid: 'var a; var b; proc foo() { b = (a = 5); }'.
	self assertInvalid: 'var a; proc foo() { if (a = 5) { turnOff(D13); }}'.