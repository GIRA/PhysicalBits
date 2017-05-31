visiting
visitLogicalOrNode: node
	^ (node class
		left: (self visit: node left)
		right: (self visit: node right))
		token: node token