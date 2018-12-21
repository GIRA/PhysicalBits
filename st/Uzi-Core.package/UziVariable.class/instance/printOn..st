printing
printOn: aStream
	super printOn: aStream.
	aStream nextPut: $(.
	self name
		ifNil: [aStream print: self value]
		ifNotNil: [aStream 
			nextPutAll: self displayName;
			nextPutAll: ': ';
			print: self value].
	aStream nextPut: $)