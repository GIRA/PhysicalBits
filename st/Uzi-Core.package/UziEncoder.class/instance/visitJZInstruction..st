encoding instructions
visitJZInstruction: instruction
	stream 
		nextPut: 16rF1;
		nextPut: (self asTwoComplement: instruction argument)