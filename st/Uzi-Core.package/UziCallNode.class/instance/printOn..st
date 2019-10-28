printing
printOn: aStream
	super printOn: aStream.
	aStream 
		nextPut: $(;
		print: selector.
	primitiveName 
		ifNotNil: [:p | aStream nextPutAll: ', '; print: p].
	aStream nextPut: $)