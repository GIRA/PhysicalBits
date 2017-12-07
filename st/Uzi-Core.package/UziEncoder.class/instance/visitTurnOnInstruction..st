encoding instructions
visitTurnOnInstruction: instruction
	instruction argument > 16r1F ifTrue: [self halt].
	stream nextPut: (16r00 bitOr: instruction argument)