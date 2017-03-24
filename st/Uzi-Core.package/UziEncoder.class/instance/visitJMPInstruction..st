encoding instructions
visitJMPInstruction: instruction
	stream 
		nextPut: 16rFF;
		nextPut: (self asTwoComplement: instruction argument)