private
escapeIfNecessary: byte
	(byte = self beginMark or: [byte = self endMark])
		ifTrue: [writer nextPut: byte]
		