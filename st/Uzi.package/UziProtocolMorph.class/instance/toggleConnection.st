button actions
toggleConnection
	self isConnected 
		ifTrue: [self disconnect]
		ifFalse: [self connect]