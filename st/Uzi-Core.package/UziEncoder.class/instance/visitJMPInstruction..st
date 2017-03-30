encoding instructions
visitJMPInstruction: instruction
	stream 
		nextPut: 16rF0;
		nextPut: (self asTwoComplement: instruction argument)