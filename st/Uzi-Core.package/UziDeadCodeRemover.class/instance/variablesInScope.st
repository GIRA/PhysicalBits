accessing
variablesInScope
	^ Array streamContents: [:stream |
		path overlappingPairsDo: [:first :second | 
			stream nextPutAll: (second variableDeclarationsUpTo: first)]]