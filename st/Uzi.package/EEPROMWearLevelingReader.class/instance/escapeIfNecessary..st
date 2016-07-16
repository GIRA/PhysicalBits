reading
escapeIfNecessary: byte
	((byte = self beginMark and: [reader peek = self beginMark])
	or: [byte = self endMark and: [reader peek = self endMark]])
		ifTrue: [reader next].
	^ byte
		