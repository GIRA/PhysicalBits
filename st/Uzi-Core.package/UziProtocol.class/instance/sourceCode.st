accessing
sourceCode
	^ currentProgram ifNil: '' ifNotNil: [:p | p sourceCode]