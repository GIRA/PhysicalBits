printing
printOn: aStream
	super printOn: aStream.
	aStream nextPut: $(;
		print: (name 
			ifNil: value 
			ifNotNil: [name -> value]);
		nextPut: $)