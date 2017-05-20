accessing
intervalAt: pc
	^ intervals
		ifNotNil: [:arr | arr at: pc + 1 ifAbsent: [nil]]