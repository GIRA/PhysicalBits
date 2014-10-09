encoding scripts
writeVariables: anUziScript
	stream nextPut: anUziScript variables size.
	anUziScript variables ifEmpty: [^ self].
	self halt