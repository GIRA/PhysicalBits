grammar-variables
variable
	^ identifier token 
		map: [:token || varName |
			varName := token parsedValue. 
			(UziVariableNode named: varName asSymbol) token: token]