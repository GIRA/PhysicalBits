instance creation
alias: aliasString path: pathString initializationBlock: aBlockOrNil isResolved: isResolvedBoolean
	^ self basicNew 
		initializeWithAlias: aliasString 
		path: pathString
		initializationBlock: aBlockOrNil
		isResolved: isResolvedBoolean
	