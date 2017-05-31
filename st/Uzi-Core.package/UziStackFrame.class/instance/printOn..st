printing
printOn: aStream
	aStream nextPutAll: script name.
	script arguments do: [:arg |
		aStream space;
			nextPutAll: arg name;
			nextPutAll: ': ';
			print: (locals at: arg ifAbsent: ['*error*'])]