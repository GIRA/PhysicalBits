comparing
= anObject 
	(anObject isKindOf: UziPrimitiveCallNode) ifFalse: [^ false].
	^ self selector = anObject selector
		and: [self primitive = anObject primitive
		and: [self arguments hasEqualElements: anObject arguments]]