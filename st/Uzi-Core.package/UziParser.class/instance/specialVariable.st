grammar-variables
specialVariable
	^ '#' asParser, identifier token 
		map: [:ign :token || varName primitive |
			varName := token parsedValue. 
			primitive := self primitiveForSpecialVariable: varName.
			(UziSpecialVariableNode 
				name: varName asSymbol
				primitive: primitive) 
				token: token]