accessing
textualForm
	^ debugger program
		ifNil: ['']
		ifNotNil: [:program | printer print: program]