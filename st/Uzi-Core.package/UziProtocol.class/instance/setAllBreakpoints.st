debugging
setAllBreakpoints
	^ self send: { 
		self class msgOutDebugSetBreakpointsAll.
		1
	}