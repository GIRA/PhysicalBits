tests
testLoopConditionShouldBeABlock
	self assertInvalid: 'task foo() { while turnOn(D13) { turnOff(D13); }}'.
	self assertInvalid: 'var a; task foo() { while a = 3 { turnOff(D13); }}'.
	"self assertInvalid: 'var a; task foo() { do { turnOff(D13); } while a; }'"