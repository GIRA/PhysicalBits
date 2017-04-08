grammar-blocks
block
	^ ($[ asParser , ws , argumentList optional, ws, statements , ws , $] asParser) token
		map: [:token || args stmts |
			args := (token parsedValue at: 3) ifNil: [#()].
			stmts := token parsedValue at: 5.
			(UziBlockNode arguments: args statements: stmts) token: token]