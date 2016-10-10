encoding instructions
writeInstructions: anUziScript
	stream nextPut: anUziScript instructions size.
	self visitCollection: anUziScript instructions
	