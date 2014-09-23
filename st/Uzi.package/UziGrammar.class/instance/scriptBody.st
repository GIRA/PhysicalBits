grammar-scripts
scriptBody
	^ $[ asParser uziToken , 
		(statements ==> [:nodesAndTokens |
			nodesAndTokens reject: [:each | each isNil or: [each isKindOf: UziToken]]]), 
		$] asParser uziToken