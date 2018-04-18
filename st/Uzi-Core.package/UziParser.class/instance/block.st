grammar-block
block
	^ ws , 
	(${ asParser, statementsList , $} asParser) token,
	 ws
	map: [:ign1 :token :ign2 || stmts |
		stmts := token parsedValue at: 2.
		(UziBlockNode statements: stmts) token: token]