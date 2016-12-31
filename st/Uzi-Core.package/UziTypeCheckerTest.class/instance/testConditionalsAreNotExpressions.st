tests
testConditionalsAreNotExpressions
	self assertInvalid: '#foo [D13 write: (3 > 4 ifTrue: [D13 off])]'.
	self assertInvalid: '#foo [a := 3 > 4 ifTrue: [D13 off]]'.