initializing
initialize
	super initialize.
	program := UziDebuggerProgram on: protocol currentProgram.
	userBreakpoints := Set new.
	debuggerBreakpoints := Set new.
	protocol 
		when: self coroutineUpdateEvent 
		send: #update:pc:fp:stack:
		to: self