button actions
openDebugger
	self isConnected ifFalse: [^ self].
	self chooseBreakpoints ifNotEmpty: [:breakpoints | 
		protocol openDebugger ifNotNil: [:debugger |
			protocol setBreakpoints: breakpoints.
			(UziDebuggerMorph debugger: debugger) openInWindow]]