visiting
visitProgramNode: node
	program := node.
	
	node globals do: [:each | 
		self assertVariableDeclaration: each.
		each value ifNotNil: [:v | self assertNumber: v]].
	node scripts do: [:each | self assertScript: each].
		
	self visitCollection: node globals;
		visitCollection: node scripts 
	