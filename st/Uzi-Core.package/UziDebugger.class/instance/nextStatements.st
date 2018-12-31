accessing
nextStatements
	^ self currentInstruction ifNotNil: [:current |
		current node ifNotNil: [:node |
			node nextStatements]]
	