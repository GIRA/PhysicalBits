copying
copyWithoutLastStatement
	^ (self class 
		arguments: arguments
		statements: statements allButLast)
			token: token