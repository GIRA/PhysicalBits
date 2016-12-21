parsing
parseOn: aStream
	| position result |
	position := aStream position.
	result := parser parseOn: aStream.
	(result isPetitFailure or: [ aStream atEnd ])
		ifTrue: [ ^ result ].
	result := PPFailure
		message: 'end of input expected'
		at: aStream position.
	aStream position: position.
	^ result