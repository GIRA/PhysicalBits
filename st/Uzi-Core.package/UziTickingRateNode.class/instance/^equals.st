comparing
= anObject 
	(anObject isKindOf: UziTickingRateNode) ifFalse: [^ false].
	^ self value = anObject value
		and: [self scale = anObject scale]