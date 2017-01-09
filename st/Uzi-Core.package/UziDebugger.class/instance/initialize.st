initializing
initialize 
	super initialize.
	protocol 
		when: self coroutineUpdateEvent 
		send: #update:pc:stack:
		to: self