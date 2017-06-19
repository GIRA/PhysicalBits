visiting
visitProgramNode: node
	program := node.
	^ (node class
		variableDeclarations: (self visitCollection: node variableDeclarations) 
		scripts: (self visitCollection: node scripts))
		token: node token