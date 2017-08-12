comparing
= anObject
	(anObject isKindOf: UziLogicalAndNode) ifFalse: [^ false].
	^ self left = anObject left and: [self right = anObject right]