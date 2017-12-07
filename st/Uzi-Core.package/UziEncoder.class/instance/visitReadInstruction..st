encoding instructions
visitReadInstruction: instruction
	instruction argument > 16r1F ifTrue: [self halt].
	stream nextPut: (16r60 bitOr: instruction argument)