instance creation
selector: aSymbol arguments: anArray primitiveName: primName
	^ self basicNew 
		initializeWithSelector: aSymbol
		arguments: anArray
		primitiveName: primName