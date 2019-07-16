accessing
sourceCode
	^ runningProgram ifNil: '' ifNotNil: [:p | p sourceCode]