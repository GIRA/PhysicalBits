visiting
visitProgramNode: node
	program := node.
	
	node variableDeclarations do: [:each | self assertVariableDeclaration: each].
	node scripts do: [:each | self assertScript: each].
		
	self visitCollection: node variableDeclarations;
		visitCollection: node scripts 
	