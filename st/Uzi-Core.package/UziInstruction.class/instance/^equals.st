comparing
= anObject
	self == anObject ifTrue: [^ true].
	^ self class = anObject class and: [self argument = anObject argument]