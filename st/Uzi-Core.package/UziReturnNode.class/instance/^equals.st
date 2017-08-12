comparing
= anObject 
	(anObject isKindOf: UziReturnNode) ifFalse: [^ false].
	^ self value = anObject value