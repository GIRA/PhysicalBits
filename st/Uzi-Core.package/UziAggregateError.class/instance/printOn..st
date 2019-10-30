printing
printOn: aStream
	aStream nextPutAll: self messageText; cr.
	errors withIndexDo: [:err :index |
		index > 1 ifTrue: [aStream cr].
		aStream 
			print: index;
			nextPutAll: '. ';
			print: err]