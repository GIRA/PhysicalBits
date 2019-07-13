encoding instructions
writeInstructions: anUziScript
	self flag: #Richo. "TODO: What happens if my script has more than 
	256 instructions?"
	anUziScript instructions size > 16rFF ifTrue: [self halt].
	stream nextPut: anUziScript instructions size.
	self visitCollection: anUziScript instructions
	