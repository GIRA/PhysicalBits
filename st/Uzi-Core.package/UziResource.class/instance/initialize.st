initialize-release
initialize
	super initialize.
	protocol 
		when: UziProtocolEvents connect 
		send: #update to: self;
		when: UziProtocolEvents disconnect 
		send: #update to: self.
	
	sema := Dictionary new.
	changed := false.
	self initializeResources