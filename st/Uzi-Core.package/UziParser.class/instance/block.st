grammar-blocks
block
	^ ($[ asParser , ws , argumentList optional, ws, variableDefinitionList, ws, statements , ws , $] asParser) token
		map: [:token || args locals stmts |
			args := (token parsedValue at: 3) ifNil: [#()].
			locals := token parsedValue at: 5.
			stmts := token parsedValue at: 7.
			(UziBlockNode arguments: args locals: locals statements: stmts) token: token]