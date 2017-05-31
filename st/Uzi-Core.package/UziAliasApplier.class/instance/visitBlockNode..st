visiting
visitBlockNode: node
	^ (node class
		arguments: (self visitCollection: node arguments)
		locals: (self visitCollection: node locals)
		statements: (self visitCollection: node statements))
		token: node token