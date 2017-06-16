accessing
textualFormSelection
	^ (printer intervalAt: (stackFrame ifNotNil: [:sf | sf pc]))
		ifNil: [1 to: 0]