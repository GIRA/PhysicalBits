tests
testScriptArgumentsAndLocalsMustNotCollide
	self assertInvalid: '@foo [:a || a | D13 off]'