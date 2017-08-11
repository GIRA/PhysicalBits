visiting
visitProgramNode: node
	program := node.
	
	node variableDeclarations 
		do: [:each | 
			self assertVariableDeclaration: each.
			each value ifNotNil: [:v | self assertNumber: v]].
	node scripts do: [:each | self assertScript: each].
		
	self visitCollection: node variableDeclarations;
		visitCollection: node scripts 
	