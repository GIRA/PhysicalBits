parsing
parseOn: aStream
	^ (aStream atEnd not and: [ literal = aStream uncheckedPeek ])
		ifFalse: [ PPFailure message: message at: aStream position ]
		ifTrue: [ aStream next ]