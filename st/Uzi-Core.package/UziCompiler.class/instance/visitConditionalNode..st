visiting
visitConditionalNode: node
	node falseBranch isNil ifTrue: [^ self handleIfTrue: node].
	node trueBranch isNil ifTrue: [^ self handleIfFalse: node].
	^ self handleIfTrueIfFalse: node