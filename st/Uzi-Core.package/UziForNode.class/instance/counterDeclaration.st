accessing
counterDeclaration
	^ (UziVariableDeclarationNode 
		name: self counter name 
		value: self start)
			token: self counter token