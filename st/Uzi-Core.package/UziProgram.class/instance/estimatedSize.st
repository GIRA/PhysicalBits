accessing
estimatedSize
	^ 10 + 
	(self allGlobals size * 5) +
	(self scripts collect: #estimatedSize) sum