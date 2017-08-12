comparing
= anObject
	(anObject isKindOf: UziScriptNode) ifFalse: [^ false].
	^ self name = anObject name
		and: [(self arguments hasEqualElements: anObject arguments)
		and: [self state = anObject state
		and: [self tickingRate = anObject tickingRate 
		and: [self body = anObject body]]]]