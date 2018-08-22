visiting
visitForNode: node
	node step isNumberLiteral
		ifTrue: [self handleForLoopWithConstantStep: node]
		ifFalse: [self handleForLoop: node]