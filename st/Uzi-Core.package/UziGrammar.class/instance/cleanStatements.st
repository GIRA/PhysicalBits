grammar
cleanStatements
	^ (statements ==> [:nodesAndTokens |
		nodesAndTokens reject: [:each | each isNil or: [each isKindOf: UziToken]]])