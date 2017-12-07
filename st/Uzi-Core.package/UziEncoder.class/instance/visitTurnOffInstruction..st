encoding instructions
visitTurnOffInstruction: instruction
	instruction argument > 16r1F ifTrue: [self halt].
	stream nextPut: (16r20 bitOr: instruction argument)