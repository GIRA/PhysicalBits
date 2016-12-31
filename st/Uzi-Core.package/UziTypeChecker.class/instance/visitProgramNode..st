visiting
visitProgramNode: node
	node variableDefinitions 
		do: [:each | self assert: each isVariableDefinition description: 'Variable definition expected' node: each].
	node scripts
		do: [:each | self assert: each isScript description: 'Script expected' node: each].
		
	self visitCollection: node variableDefinitions;
		visitCollection: node scripts 
	