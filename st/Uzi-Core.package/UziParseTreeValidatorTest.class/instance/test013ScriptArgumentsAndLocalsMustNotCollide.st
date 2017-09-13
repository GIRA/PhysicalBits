tests
test013ScriptArgumentsAndLocalsMustNotCollide
	self assertInvalid: 'task foo(a) stopped { var a; turnOff(D13); }'