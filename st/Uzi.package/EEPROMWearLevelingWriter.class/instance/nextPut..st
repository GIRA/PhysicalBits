writing
nextPut: byte
	self escapeIfNecessary: byte.
	^ self privateNextPut: byte