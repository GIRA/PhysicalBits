comparing
= anObject 
	(anObject isKindOf: UziVariableNode) ifFalse: [^ false].
	^ self name = anObject name