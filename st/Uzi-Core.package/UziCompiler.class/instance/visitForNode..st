visiting
visitForNode: node
	(node step isNumberLiteral or: [node step isPinLiteral])
		ifTrue: [self handleForLoopWithConstantStep: node]
		ifFalse: [self handleForLoop: node]