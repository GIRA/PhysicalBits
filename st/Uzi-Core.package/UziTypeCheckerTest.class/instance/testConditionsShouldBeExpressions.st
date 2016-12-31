tests
testConditionsShouldBeExpressions
	self assertInvalid: '#foo [D13 on ifTrue: [D13 off]]'.
	self assertInvalid: '#foo [(a := 3) ifTrue: [D13 off]]'.