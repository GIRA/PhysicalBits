visiting
visitBlockNode: node
	^ (node class
		locals: (self visitCollection: node locals)
		statements: (self visitCollection: node statements))
		token: node token