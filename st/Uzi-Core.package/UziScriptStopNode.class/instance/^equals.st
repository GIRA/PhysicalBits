comparing
= anObject 
	(anObject isKindOf: UziScriptStopNode) ifFalse: [^ false].
	^ self scriptNames = anObject scriptNames