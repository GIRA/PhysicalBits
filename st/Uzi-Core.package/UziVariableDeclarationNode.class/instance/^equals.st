comparing
= anObject 
	(anObject isKindOf: UziVariableDeclarationNode) ifFalse: [^ false].
	^ self name = anObject name
		and: [self value = anObject value]