accessing
stackString
	^ self stackFrame 
		ifNil: [stackString] 
		ifNotNil: [:sf | stackString := sf annotatedStack]