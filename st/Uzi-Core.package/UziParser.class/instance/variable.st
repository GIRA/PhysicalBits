grammar-variables
variable
	^ ws , (identifier token) , ws
		map: [:ign1 :token :ign2 || varName |
			varName := token parsedValue. 
			(UziVariableNode named: varName asSymbol) token: token]