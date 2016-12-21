parsing
parseOn: aStream
	| element |
	^ (element := parser parseOn: aStream) isPetitFailure
		ifFalse: [ block value: element ]
		ifTrue: [ element ]