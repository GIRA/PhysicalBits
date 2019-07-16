button actions
openDebugger
	self isConnected ifFalse: [^ self].
	self chooseBreakpoints ifNotEmpty: [:breakpoints | 
		controller debugger ifNotNil: [:debugger |
			debugger setUserBreakpoints: breakpoints;
				sendBreakpoints.
			(UziDebuggerMorph debugger: debugger) openInWindow]]