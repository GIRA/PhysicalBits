tests
testConditionalsAreNotExpressions
	"self assertInvalid: 'task foo() { write(D13, if 3 > 4 { turnOff(D13); }); }'."