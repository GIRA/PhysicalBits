comparing
= anObject 
	(anObject isKindOf: UziScriptControlNode) ifFalse: [^ false].
	^ self scripts = anObject scripts