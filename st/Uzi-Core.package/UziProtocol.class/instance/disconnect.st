connection
disconnect
	self isConnected ifFalse: [^ self].
	[self stopReporting]
		ensure: [self privateDisconnect]