initialization
initializeOn: aBlock message: aString negated: aNegatedBlock message: aNegatedString
	predicate := aBlock.
	predicateMessage := aString.
	negated := aNegatedBlock.
	negatedMessage := aNegatedString