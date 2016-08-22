encoding instructions
visitWriteInstruction: instruction
	stream nextPut: (16r40 bitOr: instruction argument)