copying
copyWithoutLastStatement
	^ (self class 
		arguments: arguments
		locals: locals
		statements: statements allButLast)
			token: token