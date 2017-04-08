encoding scripts
writeLocals: anUziScript
	anUziScript hasLocals 
		ifTrue: [stream nextPut: anUziScript locals size]