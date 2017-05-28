accessing
variables
	^ currentProgram
		ifNil: [#()]
		ifNotNil: [currentProgram sortedGlobals 
			select: [:var | var name notNil]
			thenCollect: [:var | var name]]