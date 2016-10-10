literals
variableDefinition 
	^ super variableDefinition
		map: [:var :ign :val :sep | UziVariableDefinitionNode name: var name value: val value]