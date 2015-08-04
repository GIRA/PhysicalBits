comparing
= anObject
	self == anObject ifTrue: [^ true].
	^ (anObject isKindOf: self class) and: [self code = anObject code]