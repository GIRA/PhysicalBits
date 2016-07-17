keep alive
keepAlive
	self isConnected ifFalse: [self stopKeepAliveProcess].
	^ self send: {7}