encoding instructions
visitTurnOffInstruction: instruction
	stream nextPut: (16r20 bitOr: instruction argument)