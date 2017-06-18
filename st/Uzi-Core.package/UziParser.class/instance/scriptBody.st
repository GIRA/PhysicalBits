grammar-scripts
scriptBody
	^ ws , 
	(${ asParser , statementsList , $} asParser) token,
	 ws
	map: [:ign1 :token :ign2 || stmts |
		stmts := token parsedValue second.
		(UziBlockNode 
			locals: #()
			statements: stmts) 
			token: token]