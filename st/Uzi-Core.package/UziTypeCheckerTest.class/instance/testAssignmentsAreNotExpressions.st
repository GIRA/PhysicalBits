tests
testAssignmentsAreNotExpressions
	self assertInvalid: '#foo [D13 write: (a := 5)]'.
	self assertInvalid: '#foo [b := (a := 5)]'.
	self assertInvalid: '#foo [(a := 5) ifTrue: [D13 off]]'.