accessing
sourceCodeSelection
	^ self currentInstruction
		ifNil: [1 to: 0]
		ifNotNil: [:instruction | instruction node
			ifNil: [1 to: 0]
			ifNotNil: [:node | node interval]]