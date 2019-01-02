printing
printOn: aStream
	super printOn: aStream.
	aStream
		nextPut: $(;
		nextPutAll: innerInstructions asCommaString;
		nextPut: $)