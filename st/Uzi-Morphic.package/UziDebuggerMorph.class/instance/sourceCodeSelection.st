accessing
sourceCodeSelection
	^ (self currentInstruction
		ifNotNil: [:instruction | instruction node
		ifNotNil: [:node | node interval]])
		ifNil: [1 to: 0]