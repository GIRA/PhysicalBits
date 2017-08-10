grammar-expressions
expression 
	^ ws, (($! asParser, ws) optional, (binary / expressionNotBinary)) token
		map: [:ign :token || not expr |
			not := token parsedValue first.
			expr := token parsedValue second.
			not ifNil: [expr]
				ifNotNil: [(UziPrimitiveCallNode 
					selector: #not 
					arguments: { expr } 
					primitive: (self primitiveForSelector: #not))
					token: token]]
		