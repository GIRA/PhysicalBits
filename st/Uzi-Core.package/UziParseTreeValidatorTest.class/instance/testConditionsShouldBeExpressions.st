tests
testConditionsShouldBeExpressions
	self assertInvalid: 'task foo() { if turnOn(D13) { turnOff(D13); }}'.
	self assertInvalid: 'var a; task foo() { if a = 3 { turnOff(D13); }}'.