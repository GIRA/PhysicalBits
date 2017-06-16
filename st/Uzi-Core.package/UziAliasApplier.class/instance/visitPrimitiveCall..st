visiting
visitPrimitiveCall: node
	^ (node class
		selector: node selector
		arguments: (self visitCollection: node arguments)
		primitive: node primitive)
		token: node token