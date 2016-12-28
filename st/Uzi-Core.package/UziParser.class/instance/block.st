grammar-literals
block
	^ ($[ asParser , ws , statements , ws , $] asParser) token
		map: [:token || stmts |
			stmts := token parsedValue at: 3.
			(UziBlockNode statements: stmts) token: token]