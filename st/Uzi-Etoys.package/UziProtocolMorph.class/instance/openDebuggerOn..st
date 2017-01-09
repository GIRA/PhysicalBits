button actions
openDebuggerOn: anUziScript 
	(UziDebuggerMorph 
		debugger: (protocol debuggerOn: anUziScript))
		openInWindow