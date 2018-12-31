button actions
openDebugger
	self isConnected ifFalse: [^ self].
	protocol openDebugger ifNotNil: [:debugger |
		(UziDebuggerMorph debugger: debugger) openInWindow]