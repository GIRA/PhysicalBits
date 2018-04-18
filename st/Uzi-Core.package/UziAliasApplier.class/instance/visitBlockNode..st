visiting
visitBlockNode: node
	^ (node class statements: (self visitCollection: node statements))
		token: node token