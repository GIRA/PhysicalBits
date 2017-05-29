accessing
sourceCodeSelection
	^ (stackFrame ifNotNil: [(debugger instructionAtPC: stackFrame pc)])
		ifNil: [1 to: 0]
		ifNotNil: [:instruction | instruction node
			ifNil: [1 to: 0]
			ifNotNil: [:node | node interval]]