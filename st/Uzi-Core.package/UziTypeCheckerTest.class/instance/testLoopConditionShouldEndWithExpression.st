tests
testLoopConditionShouldEndWithExpression
	self assertInvalid: '@foo [[D13 on] whileTrue]'.
	self assertInvalid: '@foo [[a := 3] whileFalse]'.
	self assertInvalid: '@foo [[D13 on. a := 3] doWhileFalse: [a. 1. D13 off]]'.