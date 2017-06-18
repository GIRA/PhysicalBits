copying
copyWithoutLastStatement
	^ (self class 
		locals: locals
		statements: statements allButLast)
			token: token