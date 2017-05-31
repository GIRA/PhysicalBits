visiting
visitSpecialVariableNode: node
	^ (node class 
		name: node name
		primitive: node primitive)
		token: node token