grammar-start
start
	^ (importsList, variableDefinitionList , script plus) token , ws end
		map: [:token :ign || imports variables scripts |
			"If any of the imports failed return the failure"
			imports := token parsedValue first.
			imports isPetitFailure ifTrue: [^ imports].
			"Collect all variables (imported first)"
			variables := Array streamContents: [:stream |
				imports do: [:p | stream nextPutAll: p variableDeclarations].
				stream nextPutAll: token parsedValue second].
			"Collect all scripts (imported first)"
			scripts := Array streamContents: [:stream |
				imports do: [:p | stream nextPutAll: p scripts].
				stream nextPutAll: token parsedValue third].
			"Assemble the parse tree"
			(UziProgramNode 
				variableDeclarations: variables
				scripts: scripts)
				token: token]