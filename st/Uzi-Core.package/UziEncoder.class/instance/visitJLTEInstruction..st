encoding instructions
visitJLTEInstruction: instruction
	stream 
		nextPut: 16rF5;
		nextPut: (self asTwoComplement: instruction argument)