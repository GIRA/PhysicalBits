accessing
variables
	^ currentProgram
		ifNil: [#()]
		ifNotNil: [currentProgram variables 
			select: [:var | var name notNil]
			thenCollect: [:var | var name]]