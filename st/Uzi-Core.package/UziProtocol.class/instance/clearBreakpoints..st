debugging
clearBreakpoints: breakpoints
	^ self send: { 
		self class msgOutDebugSetBreakpoints.
		breakpoints size.		
	}, (Array streamContents: [:stream |
		breakpoints do: [:each |
			stream nextPut: (each bitShift: -8);
				nextPut: (each bitAnd: 16rFF);
				nextPut: 0]])