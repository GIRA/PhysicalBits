initialize-release
initialize
	super initialize.	
	encoder := UziEncoder new.
	compiler := UziCompiler new.
	protocol := UziProtocol new.
	protocol 
		when: #connect send: #update to: self;
		when: #disconnect send: #update to: self.
	
	sema := Semaphore new.
	changed := false.
	self initializeResources