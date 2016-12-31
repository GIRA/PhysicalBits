instance creation
receiver: anUziParseNode selector: aSymbol arguments: anArray primitive: prim
	^ self basicNew 
		initializeWithReceiver: anUziParseNode  
		selector: aSymbol
		arguments: anArray
		primitive: prim