grammar-variables
variableDefinition
	^ ws, (variable , ws , ':=' asParser flatten, ws , (constant / number)) token , separator 
		map: [:ign1 :token :ign2 || nodes |
			nodes := token parsedValue.
			(UziVariableDefinitionNode 
				name: nodes first name 
				value: nodes last value)
				token: token]