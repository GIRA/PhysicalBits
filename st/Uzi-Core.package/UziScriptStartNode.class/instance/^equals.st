comparing
= anObject 
	(anObject isKindOf: UziScriptStartNode) ifFalse: [^ false].
	^ self scriptName = anObject scriptName