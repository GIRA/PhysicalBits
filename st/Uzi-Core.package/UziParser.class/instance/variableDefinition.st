grammar-variables
variableDefinition
	^ ws, (variable , ws , $= asParser, ws , (constant / number)) token, ws
		map: [:ign1 :token :ign2 || def |
			def := token parsedValue.
			(UziVariableDefinitionNode 
				name: def first name 
				value: def last value)
				token: token]