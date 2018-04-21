comparing
= anObject
	(anObject isKindOf: UziForNode) ifFalse: [^ false].
	^ self counter = anObject counter 
		and: [self start = anObject start
		and: [self stop = anObject stop
		and: [self step = anObject step
		and: [self body = anObject body]]]]
		