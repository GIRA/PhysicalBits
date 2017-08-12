comparing
= anObject
	(anObject isKindOf: UziImportNode) ifFalse: [^ false].
	^ self alias = anObject alias and: [self path = anObject path]