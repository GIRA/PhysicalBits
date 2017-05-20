encoding instructions
visitJMPInstruction: instruction
	self indent;
		write: 'jmp: {1}' format: { instruction argument }