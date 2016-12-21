parsing
parseOn: aStream
	| position result |
	position := aStream position.
	result := block value: aStream.
	result isPetitFailure
		ifTrue: [ aStream position: position ].
	^ result