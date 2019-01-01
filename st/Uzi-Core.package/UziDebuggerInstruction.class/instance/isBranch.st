testing
isBranch
	^ self isTrivial not and: [self instructions last isBranch]