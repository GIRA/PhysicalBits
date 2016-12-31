accessing
scripts
	^ currentProgram
		ifNil: [#()]
		ifNotNil: [currentProgram scripts]