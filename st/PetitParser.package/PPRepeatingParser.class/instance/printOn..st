printing
printOn: aStream
	super printOn: aStream.
	aStream nextPutAll: ' ['; print: min; nextPutAll: ', '; nextPutAll: (max = SmallInteger maxVal
		ifTrue: [ '*' ] ifFalse: [ max printString ]); nextPut: $]