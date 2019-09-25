instance creation
alias: aliasString path: pathString isResolved: isResolvedBoolean
	^ self alias: aliasString 
		path: pathString
		initializationBlock: nil 
		isResolved: isResolvedBoolean
	