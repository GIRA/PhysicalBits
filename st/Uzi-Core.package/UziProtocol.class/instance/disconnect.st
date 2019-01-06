connection
disconnect
	self isConnected ifFalse: [^ self].
	[debugger close.
	self stopReporting]
		ensure: [self privateDisconnect]