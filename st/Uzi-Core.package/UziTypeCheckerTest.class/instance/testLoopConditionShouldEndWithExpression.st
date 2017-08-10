tests
testLoopConditionShouldEndWithExpression
	self assertInvalid: 'task foo() { while turnOn(D13); }'.
	self assertInvalid: 'var a; task foo() { until a = 3; }'.
	"self assertInvalid: 'var a; task foo() { 
		do { turnOn(D13); a = 3; } 
		until {a; 1; turnOff(D13);}
	}'."