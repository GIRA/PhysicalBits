comparing
= anObject
	(anObject isKindOf: UziAssignmentNode) ifFalse: [^ false].
	^ self left = anObject left and: [self right = anObject right]