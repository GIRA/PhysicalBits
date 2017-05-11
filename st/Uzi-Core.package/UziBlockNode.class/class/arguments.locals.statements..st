instance creation
arguments: argsArray locals: localsArray statements: stmtsArray
	^ self basicNew 
		initializeWithArguments: argsArray
		locals: localsArray
		statements: stmtsArray