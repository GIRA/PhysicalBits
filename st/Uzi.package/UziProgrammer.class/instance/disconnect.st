connection
disconnect
	self isConnected ifFalse: [^ self].
	port close.
	port := nil