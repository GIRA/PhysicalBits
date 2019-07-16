sending
sendBreakpoints
	(debuggerBreakpoints , userBreakpoints)
		ifEmpty: [controller clearAllBreakpoints]
		ifNotEmpty: [:bpts |
			bpts size < self program allPCs size
				ifTrue: [controller clearAllBreakpoints; setBreakpoints: bpts]
				ifFalse: [controller setAllBreakpoints; 
					clearBreakpoints: (self program allPCs copyWithoutAll: bpts)]]