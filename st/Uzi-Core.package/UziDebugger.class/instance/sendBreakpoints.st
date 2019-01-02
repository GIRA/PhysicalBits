sending
sendBreakpoints
	(debuggerBreakpoints , userBreakpoints)
		ifEmpty: [protocol clearAllBreakpoints]
		ifNotEmpty: [:bpts |
			bpts size < self program allPCs size
				ifTrue: [protocol clearAllBreakpoints; setBreakpoints: bpts]
				ifFalse: [protocol setAllBreakpoints; 
					clearBreakpoints: (self program allPCs copyWithoutAll: bpts)]]