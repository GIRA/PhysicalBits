encoding instructions
visitWriteLocalInstruction: instruction
	stream
		nextPut: 16rFF;
		nextPut: (16r80 bitOr: instruction argument)