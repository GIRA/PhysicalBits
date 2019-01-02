testing
isBranch
	^ self isTrivial not and: [self innerInstructions last isBranch]