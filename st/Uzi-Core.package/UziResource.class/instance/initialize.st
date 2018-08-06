initialize-release
initialize
	super initialize.
	self protocol 
		when: UziProtocolEvents connect 
		send: #update to: self;
		when: UziProtocolEvents disconnect 
		send: #update to: self;
		when: UziProtocolEvents runProgram
		send: #update to: self.
	
	sema := Dictionary new.
	changed := false.
	self initializeResources