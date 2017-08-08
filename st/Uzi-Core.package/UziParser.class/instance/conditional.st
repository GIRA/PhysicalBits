grammar-statements
conditional
	^ ws , 
	('if' asParser , ws , expression , ws ,
	block , ws , ('else' asParser , ws , block) optional) token ,
	ws
	map: [:ign :token : ign2 || condition trueBranch falseBranch |
		condition := token parsedValue at: 3.
		trueBranch := token parsedValue at: 5.
		falseBranch := (token parsedValue at: 7)
			ifNil: [UziBlockNode empty]
			ifNotNil: [:nodes | nodes last].
		(UziConditionalNode 
			condition: condition
			true: trueBranch
			false: falseBranch) 
			token: token]