visiting
visitProgramNode: node
	program := node.
	
	node variableDefinitions do: [:each | self assertVariableDefinition: each].
	node scripts do: [:each | self assertScript: each].
		
	self visitCollection: node variableDefinitions;
		visitCollection: node scripts 
	