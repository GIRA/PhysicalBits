grammar-variables
argument
	^ ws , (identifier token) , ws
		map: [:ign1 :token :ign2 || varName |
			varName := token parsedValue. 
			(UziVariableDeclarationNode name: varName asSymbol) 
				token: token]