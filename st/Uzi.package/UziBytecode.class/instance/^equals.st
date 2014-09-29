comparing
= anObject
	self == anObject ifTrue: [^ true].
	(anObject isKindOf: UziBytecode) ifFalse: [^ false].
	^ self code = anObject code and: [self argument = anObject argument]