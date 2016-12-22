grammar-literals
block
	^ $[ asParser , ws , statements , ws , $] asParser
		map: [:ign1 :ign2 :stmts :ign3 :ign4 | UziBlockNode statements: stmts]