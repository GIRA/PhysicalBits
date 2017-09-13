tests
test006ConditionsShouldBeExpressions
	self assertInvalid: 'task foo() stopped { if turnOn(D13) { turnOff(D13); }}'.
	self assertInvalid: 'var a; task foo() stopped { if a = 3 { turnOff(D13); }}'.