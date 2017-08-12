comparing
= anObject
	(anObject isKindOf: UziConditionalNode) ifFalse: [^ false].
	^ self condition = anObject condition
		and: [self trueBranch = anObject trueBranch
		and: [self falseBranch = anObject falseBranch]]