grammar-variables
specialVariable
	^ ('#' asParser, identifier) token 
		map: [:token || varName primitive |
			varName := token parsedValue last. 
			primitive := self primitiveForSpecialVariable: varName.
			(UziSpecialVariableNode 
				name: varName asSymbol
				primitive: primitive) 
				token: token]