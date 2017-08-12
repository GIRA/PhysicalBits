comparing
= anObject
	(anObject isKindOf: UziLoopNode) ifFalse: [^ false].
	^ self pre = anObject pre
		and: [self condition = anObject condition
		and: [self post = anObject post
		and: [self negated = anObject negated]]]