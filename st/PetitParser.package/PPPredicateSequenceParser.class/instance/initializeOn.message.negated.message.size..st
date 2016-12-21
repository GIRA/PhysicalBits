initialization
initializeOn: aBlock message: aString negated: aNegatedBlock message: aNegatedString size: anInteger
	predicate := aBlock.
	predicateMessage := aString.
	negated := aNegatedBlock.
	negatedMessage := aNegatedString.
	size := anInteger 