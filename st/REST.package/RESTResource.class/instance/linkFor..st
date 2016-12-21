accessing
linkFor: anObject
	^ self 
		resourceFor: anObject
		ifPresent: #link
		ifAbsent: nil