comparing
= anObject
	(anObject isKindOf: UziLogicalOrNode) ifFalse: [^ false].
	^ self left = anObject left and: [self right = anObject right]