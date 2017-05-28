grammar-start
start
	^ (importsList, variableDefinitionList , script plus) token , ws end
		map: [:token :ign || imports variables scripts |
			imports := token parsedValue first.
			variables := Array streamContents: [:stream |
				imports do: [:p | stream nextPutAll: p variableDefinitions].
				stream nextPutAll: token parsedValue second].
			scripts := Array streamContents: [:stream |
				imports do: [:p | stream nextPutAll: p scripts].
				stream nextPutAll: token parsedValue third].
			(UziProgramNode 
				variableDefinitions: variables
				scripts: scripts)
				token: token]