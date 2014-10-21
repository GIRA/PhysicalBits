sending
send: msg
	self isConnected ifFalse: [^ self].
	serial nextPutAll: msg asByteArray