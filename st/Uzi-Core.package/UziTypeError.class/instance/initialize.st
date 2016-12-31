initializing
initialize
	super initialize.
	self messageText: ('{1} at line {2}:{3}' 
		format: { description . node token line . node token column })