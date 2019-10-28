printing
printOn: aStream
	super printOn: aStream.
	aStream 
		nextPut: $(;
		print: alias;
		nextPutAll: ' -> ';
		print: name;
		nextPut: $)