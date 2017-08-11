tests
testScriptArgumentsAndLocalsMustNotCollide
	self assertInvalid: 'task foo(a) { var a; turnOff(D13); }'