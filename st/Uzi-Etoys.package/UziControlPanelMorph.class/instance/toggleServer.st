button actions
toggleServer
	self isServerStarted
		ifTrue: [self stopServer]
		ifFalse: [self startServer]