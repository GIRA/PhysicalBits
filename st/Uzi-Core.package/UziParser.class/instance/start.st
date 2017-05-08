grammar-start
start
	^ (variableDefinitionList , script plus) token , ws end
		map: [:token :ign | 
			(UziProgramNode 
				variableDefinitions: token parsedValue first
				scripts: token parsedValue second)
				token: token]