parsing
parseOn: aStream
	| position result |
	position := aStream position.
	result := aStream next: size.
	(result size = size and: [ predicate value: result ])
		ifTrue: [ ^ result ].
	aStream position: position.
	^ PPFailure message: predicateMessage at: aStream position