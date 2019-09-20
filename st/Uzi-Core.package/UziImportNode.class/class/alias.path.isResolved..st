instance creation
alias: aliasString path: pathString isResolved: isResolvedBoolean
	^ self basicNew 
		initializeWithAlias: aliasString 
		path: pathString
		isResolved: isResolvedBoolean
	