visiting
visitCallNode: node
	node isPrimitive
		ifTrue: [self handlePrimitiveCall: node] 
		ifFalse: [self handleScriptCall: node]