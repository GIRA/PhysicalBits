encoding instructions
visitJNZInstruction: instruction
	stream
		nextPut: 16rF2;
		nextPut: instruction argument