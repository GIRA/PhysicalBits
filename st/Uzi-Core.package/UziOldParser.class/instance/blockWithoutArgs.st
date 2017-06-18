grammar-blocks
blockWithoutArgs
	^ ($[ asParser , ws , variableDefinitionList, ws, statements , ws , $] asParser) token
		map: [:token || args locals stmts |
			locals := token parsedValue at: 3.
			stmts := token parsedValue at: 5.
			(UziBlockNode locals: locals statements: stmts) token: token]