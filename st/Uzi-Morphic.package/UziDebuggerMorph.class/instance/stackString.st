accessing
stackString
	^ self stackFrame ifNil: [''] ifNotNil: [:sf | sf annotatedStack]