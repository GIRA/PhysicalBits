printing
printOn: aStream
	aStream nextPutAll: self message; nextPutAll: ' at '; print: position