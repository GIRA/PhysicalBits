printing
printOn: aStream
	super printOn: aStream.
	aStream cr.
	errors withIndexDo: [:err :index |
		index > 1 ifTrue: [aStream cr].
		aStream 
			print: index;
			nextPutAll: '. ';
			print: err]