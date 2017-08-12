comparing
= anObject 
	(anObject isKindOf: UziProgramNode) ifFalse: [^ false].
	^ (self imports hasEqualElements: anObject imports)
		and: [(self globals hasEqualElements: anObject globals)
		and: [self scripts hasEqualElements: anObject scripts]]