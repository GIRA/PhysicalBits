running programs
runOnce: aString
	self compile: ('#once ticking [{1}]' format: {aString})
		thenDo: [:program | protocol runOnce: program]