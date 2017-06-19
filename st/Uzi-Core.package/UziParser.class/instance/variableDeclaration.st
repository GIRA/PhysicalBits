grammar-variables
variableDeclaration 
	^ ws , 
	('var' asParser , ws , variable , ws , ($= asParser , (constant / number)) optional , ws , $; asParser ) token , 
	ws
	map: [:ign1 :token :ign2 || def |
		(UziVariableDeclarationNode 
			name: token parsedValue third name 
			value: (token parsedValue fifth 
				ifNil: [0] 
				ifNotNil: [:v | v last value]))
			token: token]