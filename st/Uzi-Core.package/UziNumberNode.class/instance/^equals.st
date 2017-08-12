comparing
= anObject 
	(anObject isKindOf: UziNumberNode) ifFalse: [^ false].
	^ self value = anObject value