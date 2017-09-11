comparing
= anObject 
	(anObject isKindOf: UziScriptStartNode) ifFalse: [^ false].
	^ self scripts = anObject scripts