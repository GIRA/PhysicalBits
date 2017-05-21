accessing
textualFormSelection
	^ (printer intervalAt: (stackFrame ifNotNil: [stackFrame pc]))
		ifNil: [1 to: 0]