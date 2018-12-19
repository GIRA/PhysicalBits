accessing
stack
	^ stackFrame
		ifNil: [debugger stack]
		ifNotNil: [:sf | sf stack]