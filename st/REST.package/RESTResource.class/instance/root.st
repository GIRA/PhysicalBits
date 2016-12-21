accessing
root
	^ parent ifNil: self ifNotNil: [parent root]