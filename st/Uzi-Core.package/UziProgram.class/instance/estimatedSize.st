accessing
estimatedSize
	^ 8 + 
	(self allGlobals size * 5) +
	(self scripts collect: #estimatedSize) sum