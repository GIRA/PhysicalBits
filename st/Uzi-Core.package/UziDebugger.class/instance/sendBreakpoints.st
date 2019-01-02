sending
sendBreakpoints
	(debuggerBreakpoints , userBreakpoints)
		ifEmpty: [protocol clearAllBreakpoints]
		ifNotEmpty: [:bpts |
			bpts size < program allPCs size
				ifTrue: [protocol clearAllBreakpoints; setBreakpoints: bpts]
				ifFalse: [protocol setAllBreakpoints; 
					clearBreakpoints: (program allPCs copyWithoutAll: bpts)]]