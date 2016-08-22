encoding bytecodes
writeBytecodes: anUziScript
	stream nextPut: anUziScript bytecodes size.
	self visitCollection: anUziScript bytecodes
	