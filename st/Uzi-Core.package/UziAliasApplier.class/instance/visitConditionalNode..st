visiting
visitConditionalNode: node
	^ (node class
		condition: (self visit: node condition)
		true: (self visit: node trueBranch)
		false: (self visit: node falseBranch))
		token: node token