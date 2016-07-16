reading
next
	self atEnd ifTrue: [^ nil].
	^ self escapeIfNecessary: reader next