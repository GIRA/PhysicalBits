comparing
= anObject 
	(anObject isKindOf: UziScriptStartNode) ifFalse: [^ false].
	^ self scriptNames = anObject scriptNames