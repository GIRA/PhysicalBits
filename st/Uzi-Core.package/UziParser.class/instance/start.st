grammar-start
start
	^ (variableDefinition star , script plus) end token
		map: [:token | 
			(UziProgramNode 
				variableDefinitions: token parsedValue first
				scripts: token parsedValue second)
				token: token]