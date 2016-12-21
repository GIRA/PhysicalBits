parsing
parseOn: aStream
	| start element |
	start := aStream position.
	element := parser parseOn: aStream.
	element isPetitFailure ifTrue: [ ^ element ].
	^ self on: aStream collection start: start + 1 stop: aStream position value: element