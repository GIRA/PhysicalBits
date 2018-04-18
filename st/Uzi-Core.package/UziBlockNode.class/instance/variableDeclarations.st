accessing
variableDeclarations
	^ Array streamContents: [:stream |
		self statements do: [:stmt |
			stream nextPutAll: stmt variableDeclarations]]