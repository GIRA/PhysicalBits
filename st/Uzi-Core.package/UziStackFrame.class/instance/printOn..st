printing
printOn: aStream
	aStream
		nextPut: $@;
		nextPutAll: script name.
	script arguments do: [:arg |
		aStream space;
			nextPutAll: arg name;
			nextPutAll: ': ';
			print: (locals at: arg ifAbsent: ['*error*'])]