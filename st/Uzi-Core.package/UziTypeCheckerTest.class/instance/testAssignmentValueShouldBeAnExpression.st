tests
testAssignmentValueShouldBeAnExpression
	self assertInvalid: 'var a; proc foo() { a = turnOff(D13); }'.
	"self assertInvalid: 'var b; proc foo() { b = if 3 > 4 {3} else {4}}'."