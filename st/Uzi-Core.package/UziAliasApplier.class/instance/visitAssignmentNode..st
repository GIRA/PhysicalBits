visiting
visitAssignmentNode: node
	^ (node class
		left: (self visit: node left)
		right: (self visit: node right))
		token: node token