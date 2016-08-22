encoding instructions
visitJMPInstruction: instruction
	stream 
		nextPut: 16rFF;
		nextPut: instruction argument