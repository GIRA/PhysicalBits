instance creation
arguments: argsArray statements: stmtsArray
	^ self basicNew 
		initializeWithArguments: argsArray
		statements: stmtsArray