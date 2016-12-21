accessing
summary
	^ super summary
		at: #elements put: (children collect: #summary as: Array);
		yourself 