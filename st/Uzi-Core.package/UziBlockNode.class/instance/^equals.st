comparing
= anObject
	(anObject isKindOf: UziBlockNode) ifFalse: [^ false].
	^ self statements hasEqualElements: anObject statements