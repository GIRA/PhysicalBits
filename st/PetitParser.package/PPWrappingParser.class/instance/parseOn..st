parsing
parseOn: aStream
	^ block value: aStream value: [ parser parseOn: aStream ]