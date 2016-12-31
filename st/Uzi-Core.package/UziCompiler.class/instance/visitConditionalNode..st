visiting
visitConditionalNode: node
	node falseBranch isEmpty ifTrue: [^ self handleIfTrue: node].
	node trueBranch isEmpty ifTrue: [^ self handleIfFalse: node].
	^ self handleIfTrueIfFalse: node