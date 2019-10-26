visiting
visitProgramNode: node
	program := node.
	^ (node class
		globals: (self visitCollection: node globals) 
		scripts: (self visitCollection: node scripts)
		primitives: (self visitCollection: node primitives))
		token: node token