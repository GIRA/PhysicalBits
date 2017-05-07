accessing
scriptNamed: aSymbol
	^ scripts 
		detect: [:script | script name = aSymbol]
		ifNone: [nil]