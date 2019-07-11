accessing
variables
	^ runningProgram
		ifNil: [#()]
		ifNotNil: [runningProgram allGlobals 
			select: [:var | var name notNil]
			thenCollect: [:var | var name]]