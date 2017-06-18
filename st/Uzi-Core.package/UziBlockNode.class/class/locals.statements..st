instance creation
locals: localsArray statements: stmtsArray
	^ self basicNew 
		initializeWithLocals: localsArray
		statements: stmtsArray