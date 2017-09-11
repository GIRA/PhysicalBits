comparing
= anObject 
	(anObject isKindOf: UziScriptCallNode) ifFalse: [^ false].
	^ self script = anObject script
		and: [self arguments hasEqualElements: anObject arguments]