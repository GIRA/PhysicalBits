accessing
delayMs
	^ (scale caseOf: {
		[#s] -> [1000].
	}) / value