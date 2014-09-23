instance creation
receiver: anUziParseNode selector: aSymbol arguments: anArray
	^ self basicNew 
		initializeWithReceiver: anUziParseNode  
		selector: aSymbol
		arguments: anArray