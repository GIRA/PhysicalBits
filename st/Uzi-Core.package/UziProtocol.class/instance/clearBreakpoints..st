debugging
clearBreakpoints: breakpoints
	^ self send: { 
		self class msgOutDebugSetBreakpoints.
		0.
		breakpoints size.
	}, (breakpoints gather: [:each | {
		(each bitShift: -8) bitAnd: 16rFF.
		each bitAnd: 16rFF
		}])
	