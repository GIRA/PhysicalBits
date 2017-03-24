tests
testAssignmentValueShouldBeAnExpression
	self assertInvalid: '@foo [a := D13 off]'.
	self assertInvalid: '@foo [b := 3 > 4 ifTrue: [3] ifFalse: [4]]'.