initializing
initialize 
	super initialize.
	program := protocol currentProgram.
	protocol 
		when: self coroutineUpdateEvent 
		send: #update:pc:fp:stack:
		to: self