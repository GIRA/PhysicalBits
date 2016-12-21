accessing
at: key
	^ self at: key
		ifPresent: #yourself
		ifAbsent: nil