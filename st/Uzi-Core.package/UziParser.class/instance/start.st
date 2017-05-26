grammar-start
start
	^ (importsList, variableDefinitionList , script plus) token , ws end
		map: [:token :ign | 
			(UziProgramNode 
				imports: token parsedValue first
				variableDefinitions: token parsedValue second
				scripts: token parsedValue third)
				token: token]