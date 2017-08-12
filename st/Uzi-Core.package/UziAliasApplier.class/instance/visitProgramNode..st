visiting
visitProgramNode: node
	program := node.
	^ (node class
		globals: (self visitCollection: node globals) 
		scripts: (self visitCollection: node scripts))
		token: node token