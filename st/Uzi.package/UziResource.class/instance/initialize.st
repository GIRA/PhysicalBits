initialize-release
initialize
	super initialize.	
	encoder := UziEncoder new.
	compiler := UziCompiler new.
	protocol := UziProtocol new.
	protocol 
		when: #connect send: #update to: self;
		when: #disconnect send: #update to: self.
	simulator := UziSimulator on: protocol.
	
	sema := Semaphore new.
	changed := false.
	self initializeResources