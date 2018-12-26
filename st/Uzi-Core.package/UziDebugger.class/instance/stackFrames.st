accessing
stackFrames
	^ protocol currentProgram
		ifNil: [#()]
		ifNotNil: [:p | UziStackFrame readFrom: p stack: stack pc: pc fp: fp]