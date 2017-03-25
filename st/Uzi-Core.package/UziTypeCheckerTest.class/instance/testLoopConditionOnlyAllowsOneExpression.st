tests
testLoopConditionOnlyAllowsOneExpression
	self assertInvalid: '@foo [[1. a. D13 read] whileFalse]'