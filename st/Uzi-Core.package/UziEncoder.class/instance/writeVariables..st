encoding scripts
writeVariables: anUziScript
	self flag: #Richo. "For now we treat both args and locals as if they were the same"
	anUziScript hasVariables 
		ifTrue: [stream nextPut: anUziScript variables size]