actions
clearDebuggerBreakpoints
	protocol clearBreakpoints: (debuggerBreakpoints copyWithout: userBreakpoints).
	debuggerBreakpoints removeAll