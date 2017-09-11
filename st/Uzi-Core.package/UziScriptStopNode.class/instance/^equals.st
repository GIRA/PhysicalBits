comparing
= anObject 
	(anObject isKindOf: UziScriptStopNode) ifFalse: [^ false].
	^ self scripts = anObject scripts