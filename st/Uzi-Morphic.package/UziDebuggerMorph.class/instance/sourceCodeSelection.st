accessing
sourceCodeSelection
	^ (self currentInstruction
		ifNotNil: [:instruction | instruction sourceCodeInterval])
		ifNil: [1 to: 0]