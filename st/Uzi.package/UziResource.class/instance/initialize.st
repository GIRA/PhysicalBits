initialize-release
initialize
	super initialize.	
	encoder := UziEncoder new.
	compiler := UziCompiler new.
	protocol := UziProtocol new.
	simulator := UziSimulator on: protocol.
	self initializeResources