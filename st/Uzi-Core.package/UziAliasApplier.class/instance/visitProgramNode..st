visiting
visitProgramNode: node
	program := node.
	^ (node class
		variableDefinitions: (self visitCollection: node variableDefinitions) 
		scripts: (self visitCollection: node scripts))
		token: node token