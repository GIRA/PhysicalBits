writing
nextPut: byte
	self escapeIfNecessary: byte.
	^ writer nextPut: byte