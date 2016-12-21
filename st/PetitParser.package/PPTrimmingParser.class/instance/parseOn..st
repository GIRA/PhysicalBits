parsing
parseOn: aStream
	| position element |
	position := aStream position.
	[ (trimmer parseOn: aStream) isPetitFailure ]
		whileFalse.
	element := parser parseOn: aStream.
	element isPetitFailure ifTrue: [
		aStream position: position.
		^ element ].
	[ (trimmer parseOn: aStream) isPetitFailure ]
		whileFalse.
	^ element