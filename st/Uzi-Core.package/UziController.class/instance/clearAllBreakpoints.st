debugging
clearAllBreakpoints
	^ self send: { 
		self class msgOutDebugSetBreakpointsAll.
		0
	}