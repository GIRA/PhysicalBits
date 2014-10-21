reading
next
	buffer atEnd
		ifTrue: [self fillBuffer].
	^ buffer next