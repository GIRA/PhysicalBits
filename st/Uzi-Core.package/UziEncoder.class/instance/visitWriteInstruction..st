encoding instructions
visitWriteInstruction: instruction
	instruction argument > 16r1F ifTrue: [self halt].
	stream nextPut: (16r40 bitOr: instruction argument)