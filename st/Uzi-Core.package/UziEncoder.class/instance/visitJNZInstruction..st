encoding instructions
visitJNZInstruction: instruction
	stream
		nextPut: 16rF2;
		nextPut: (self asTwoComplement: instruction argument)