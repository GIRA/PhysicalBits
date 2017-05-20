accessing
intervalAt: pc
	pc ifNil: [^ nil].
	^ intervals
		ifNotNil: [:arr | arr at: pc + 1 ifAbsent: [nil]]