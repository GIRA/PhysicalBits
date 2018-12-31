button actions
openDebugger
	self isConnected ifFalse: [^ self].
	self chooseBreakpoints ifNotEmpty: [:breakpoints | 
		protocol openDebugger ifNotNil: [:debugger |
			debugger setUserBreakpoints: breakpoints.
			(UziDebuggerMorph debugger: debugger) openInWindow]]