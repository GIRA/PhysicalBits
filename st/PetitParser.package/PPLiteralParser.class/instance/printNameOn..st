printing
printNameOn: aStream
	super printNameOn: aStream.
	aStream nextPutAll: ', '; print: literal