grammar-block
block
	^ ws , 
	(${ asParser, variableDeclaration star , statementsList , $} asParser) token,
	 ws
	map: [:ign1 :token :ign2 || locals stmts |
		locals := token parsedValue at: 2.
		stmts := token parsedValue at: 3.
		(UziBlockNode 
			locals: locals
			statements: stmts) 
			token: token]