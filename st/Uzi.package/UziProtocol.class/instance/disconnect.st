connection
disconnect
	self isConnected ifFalse: [^ self].
	[self stopReporting]
		ensure: [self stopSteppingProcess.
			serial close.
			serial := nil]