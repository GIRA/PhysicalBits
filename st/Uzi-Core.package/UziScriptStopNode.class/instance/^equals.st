comparing
= anObject 
	(anObject isKindOf: UziScriptStopNode) ifFalse: [^ false].
	^ self scriptName = anObject scriptName