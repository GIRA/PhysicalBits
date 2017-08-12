comparing
= anObject 
	(anObject isKindOf: UziScriptCallNode) ifFalse: [^ false].
	^ self scriptName = anObject scriptName
		and: [self arguments hasEqualElements: anObject arguments]