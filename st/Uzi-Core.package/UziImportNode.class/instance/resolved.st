converting
resolved
	^ (self class 
		alias: alias 
		path: path 
		initializationBlock: initializationBlock 
		isResolved: true) 
			token: token