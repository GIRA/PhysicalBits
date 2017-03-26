tests
testMessageSendReceiverShouldBeAnExpression
	"self assertInvalid: '@foo [(a := 5) on]'.
	self assertInvalid: '@foo [[D13 on] on]'."
	self assertInvalid: '@foo [D13 on on]'