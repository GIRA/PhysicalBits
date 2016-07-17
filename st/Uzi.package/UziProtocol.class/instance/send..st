sending
send: msg
	| bytes |
	bytes := msg asByteArray.
	self isConnected ifFalse: [^ bytes].
	(serial nextPutAll: bytes) < 0
		ifTrue: [
			"An error occurred, maybe the board was unplugged.
			Just in case, disconnect"
			self privateDisconnect].
	^ bytes