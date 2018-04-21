comparing
= anObject
	(anObject isKindOf: UziForeverNode) ifFalse: [^ false].
	^ self body = anObject body