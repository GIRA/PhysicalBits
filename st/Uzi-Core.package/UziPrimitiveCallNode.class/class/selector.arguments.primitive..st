instance creation
selector: aSymbol arguments: anArray primitive: prim
	^ self basicNew 
		initializeWithSelector: aSymbol
		arguments: anArray
		primitive: prim