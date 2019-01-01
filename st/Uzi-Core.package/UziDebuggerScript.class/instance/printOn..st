printing
printOn: aStream
	super printOn: aStream.
	aStream
		nextPut: $(;
		print: self scriptName;
		nextPut: $)