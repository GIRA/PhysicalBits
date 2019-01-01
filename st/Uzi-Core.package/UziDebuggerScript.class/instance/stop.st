accessing
stop
	^ self instructions 
		ifEmpty: [self start]
		ifNotEmpty: [:inst | inst last stop]