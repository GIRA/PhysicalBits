comparing
= anObject 
	(anObject isKindOf: UziScriptRefNode) ifFalse: [^ false].
	^ self scriptName = anObject scriptName