initializing
initialize
	super initialize.
	program := protocol currentProgram.
	debuggerProgram := UziDebuggerProgram on: protocol currentProgram.
	userBreakpoints := Set new.
	debuggerBreakpoints := Set new.
	protocol 
		when: self coroutineUpdateEvent 
		send: #update:pc:fp:stack:
		to: self