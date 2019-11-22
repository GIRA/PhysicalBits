encoding instructions
writeInstructions: anUziScript
	| instructionCount |
	instructionCount := anUziScript instructions size.
	instructionCount > 16r7FFF ifTrue: [self halt].
	instructionCount > 16r7F 
		ifTrue: [
			stream nextPut: ((instructionCount bitShift: -8) bitOr: 16r80).
			stream nextPut: (instructionCount bitAnd: 16rFF)]
		ifFalse: [stream nextPut: instructionCount].
	self visitCollection: anUziScript instructions