tests
testMessageSendReceiverShouldBeAnExpression
	self assertInvalid: 'var a; task foo() { turnOn(a = 5); }'.
	self assertInvalid: 'task foo() { turnOn(turnOn(D13)); }'.
	"self assertInvalid: 'task foo() { turnOn({turnOn(D13);});}'"