grammar-variables
variableDeclaration 
	^ ws , 
	('var' asParser , ws , variable , ws , ($= asParser , expression) optional , ws , $; asParser ) token , 
	ws
	map: [:ign1 :token :ign2 || def |
		(UziVariableDeclarationNode 
			name: token parsedValue third name 
			value: (token parsedValue fifth
				ifNotNil: [:v | v last]))
			token: token]