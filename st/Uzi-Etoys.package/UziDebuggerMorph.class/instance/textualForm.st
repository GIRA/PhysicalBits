accessing
textualForm
	^ debugger currentProgram
		ifNil: ['']
		ifNotNil: [:program | printer print: program]