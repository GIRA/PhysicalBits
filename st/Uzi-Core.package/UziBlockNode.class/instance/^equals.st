comparing
= anObject
	(anObject isKindOf: UziBlockNode) ifFalse: [^ false].
	^ (self locals hasEqualElements: anObject locals)
		and: [self statements hasEqualElements: anObject statements]