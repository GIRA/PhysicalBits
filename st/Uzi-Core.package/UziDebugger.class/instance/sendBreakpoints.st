sending
sendBreakpoints
	(debuggerBreakpoints , userBreakpoints)
		ifEmpty: [protocol clearAllBreakpoints]
		ifNotEmpty: [:bpts |
			bpts size < program instructions size
				ifTrue: [protocol clearAllBreakpoints; setBreakpoints: bpts]
				ifFalse: [protocol setAllBreakpoints; 
					clearBreakpoints: (self allPCs copyWithoutAll: bpts)]]