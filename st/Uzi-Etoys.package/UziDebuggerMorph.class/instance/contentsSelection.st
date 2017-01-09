accessing
contentsSelection
	^ debugger currentInstruction
		ifNil: [1 to: 0]
		ifNotNil: [:instruction | instruction node interval]