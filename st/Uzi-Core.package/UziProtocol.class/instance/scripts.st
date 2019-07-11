accessing
scripts
	^ runningProgram
		ifNil: [#()]
		ifNotNil: [:p | p scripts]