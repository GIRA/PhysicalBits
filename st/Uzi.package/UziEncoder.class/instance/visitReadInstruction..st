encoding instructions
visitReadInstruction: instruction
	stream nextPut: (16r60 bitOr: instruction argument)