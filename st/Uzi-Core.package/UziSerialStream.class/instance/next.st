reading
next
	| tmp |
	buffer atEnd
		ifTrue: [self fillBuffer].
	tmp := buffer next.
	self class debugEnabled 
		ifTrue: [UziTranscript log: 'IN: {1}' format: { tmp }].
	^ tmp