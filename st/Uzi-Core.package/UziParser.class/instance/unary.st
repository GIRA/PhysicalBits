grammar-expressions
unary
	^ ws, ($! asParser, ws, expressionNotBinary) token
		map: [:ign :token || not expr |
			not := token parsedValue first.
			expr := token parsedValue third.
			(UziPrimitiveCallNode 
				selector: #! 
				arguments: { expr } 
				primitiveName: (self primitiveForSelector: #!))
				token: token]