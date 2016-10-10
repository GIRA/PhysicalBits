writing
nextPut: byte
	self assert: self atEnd not description: 'Can''t write past the end'.
	self escapeIfNecessary: byte.
	^ writer nextPut: byte