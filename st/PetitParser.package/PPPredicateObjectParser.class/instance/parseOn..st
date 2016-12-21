parsing
parseOn: aStream
	^ (aStream atEnd not and: [ predicate value: aStream uncheckedPeek ])
		ifFalse: [ PPFailure message: predicateMessage at: aStream position ]
		ifTrue: [ aStream next ]