visiting
visitForNode: node
	node step isNumber 
		ifTrue: [self handleForLoopWithConstantStep: node]
		ifFalse: [self handleForLoop: node]