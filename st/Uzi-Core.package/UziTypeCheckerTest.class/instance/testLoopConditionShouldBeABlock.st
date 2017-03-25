tests
testLoopConditionShouldBeABlock
	self assertInvalid: '@foo [D13 on whileTrue: [D13 off]]'.
	self assertInvalid: '@foo [(a := 3) whileFalse: [D13 off]]'.
	self assertInvalid: '@foo [[D13 off] doWhileTrue: a]'