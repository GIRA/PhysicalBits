visiting
visitPrimitiveCall: node
	^ (node class
		selector: node selector
		arguments: (self visitCollection: node arguments)
		primitiveName: node primitiveName)
		token: node token