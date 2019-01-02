printing
printOn: aStream
	aStream 
		nextPutAll: script scriptName;
		nextPut: $(.
	script arguments do: [:arg |
		aStream
			nextPutAll: arg displayName;
			nextPutAll: ': ';
			print: (locals at: arg ifAbsent: ['*error*'])]
		separatedBy: [aStream nextPutAll: ', '].
	aStream nextPut: $).