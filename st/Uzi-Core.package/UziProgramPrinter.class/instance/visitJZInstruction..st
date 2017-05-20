encoding instructions
visitJZInstruction: instruction
	self indent;
		write: 'jz: {1}' format: { instruction argument }