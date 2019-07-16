keep alive
keepAlive
	self isConnected ifFalse: [self stopKeepAliveProcess].
	^ self send: { self class msgOutKeepAlive }