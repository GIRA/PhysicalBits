grammar-variables
specialVariable
	^ ('#' asParser, identifier) token 
		map: [:token || varName primitive |
			varName := token parsedValue last. 
			primitive := self primitiveForSpecialVariable: varName.
			(UziPrimitiveCallNode 
				selector: varName asSymbol
				arguments: #()
				primitive: primitive) 
				token: token]