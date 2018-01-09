accessing
variables
	^ currentProgram
		ifNil: [#()]
		ifNotNil: [currentProgram allGlobals 
			select: [:var | var name notNil]
			thenCollect: [:var | var name]]