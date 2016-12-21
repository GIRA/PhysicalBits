parsing
parseOn: aStream
	| position result |
	position := aStream position.
	result := aStream next: size.
	literal = result ifTrue: [ ^ result ].
	aStream position: position.
	^ PPFailure message: message at: aStream position