initialize-release
initialize
	super initialize.
	protocol := UziProtocol new.
	protocol 
		when: #connect send: #update to: self;
		when: #disconnect send: #update to: self.
	
	sema := Semaphore new.
	changed := false.
	self initializeResources