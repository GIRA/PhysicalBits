parsing
parseOn: aStream
	| element |
	element := parser parseOn: aStream.
	^ element isPetitFailure ifFalse: [ element ]