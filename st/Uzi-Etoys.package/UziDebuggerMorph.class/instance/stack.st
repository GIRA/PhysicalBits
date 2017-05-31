accessing
stack
	^ stackFrame
		ifNil: [debugger stack]
		ifNotNil: [stackFrame stack]