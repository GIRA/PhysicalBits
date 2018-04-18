accessing
variableDeclarations 
	^ Array streamContents: [:stream |
		self globals do: [:global | stream nextPutAll: global variableDeclarations].
		self scripts do: [:script | stream nextPutAll: script variableDeclarations]]