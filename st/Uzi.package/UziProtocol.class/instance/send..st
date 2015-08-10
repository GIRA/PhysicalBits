sending
send: msg
	self isConnected ifFalse: [^ self].
	(serial nextPutAll: msg asByteArray) < 0
		ifTrue: [
			"An error occurred, maybe the board was unplugged.
			Just in case, disconnect"
			self privateDisconnect]