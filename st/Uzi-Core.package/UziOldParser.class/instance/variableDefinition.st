grammar-variables
variableDefinition
	^ ws, (variable , (ws , $= asParser, ws , (constant / number)) optional) token, ws
		map: [:ign1 :token :ign2 || def |
			def := token parsedValue.
			(UziVariableDeclarationNode 
				name: def first name 
				value: (def second ifNil: [0] ifNotNil: [:v | v last value]))
				token: token]