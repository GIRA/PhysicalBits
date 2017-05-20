accessing
textualFormSelection
	^ (printer intervalAt: debugger pc)
		ifNil: [1 to: 0]