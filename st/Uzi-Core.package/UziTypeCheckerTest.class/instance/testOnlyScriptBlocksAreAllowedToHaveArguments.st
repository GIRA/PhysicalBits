tests
testOnlyScriptBlocksAreAllowedToHaveArguments
	self assertInvalid: '@foo [D13 read ifTrue: [:a | D13 toggle]]'.
	self assertInvalid: '@foo [D13 read ifFalse: [:a | D13 toggle]]'.
	self assertInvalid: '@foo [[D13 read] whileTrue: [:a | D13 toggle]]'.
	self assertInvalid: '@foo [[:a | D13 read] whileTrue: [D13 toggle]]'.
	self assertInvalid: '@foo [[:a | D13 toggle] doWhileTrue: [D13 read]]'.