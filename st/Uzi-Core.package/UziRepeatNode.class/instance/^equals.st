comparing
= anObject
	(anObject isKindOf: UziRepeatNode) ifFalse: [^ false].
	^ self times = anObject times
		and: [self body = anObject body]