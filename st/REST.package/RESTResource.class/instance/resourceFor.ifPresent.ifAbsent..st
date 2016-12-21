accessing
resourceFor: anObject ifPresent: presentBlock ifAbsent: absentBlock
	^ (self resourceFor: anObject)
		ifNil: absentBlock
		ifNotNil: presentBlock