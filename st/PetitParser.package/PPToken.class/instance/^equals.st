comparing
= anObject
	^ self class = anObject class and: [ self parsedValue = anObject parsedValue ]