encoding instructions
visitWriteInstruction: instruction
	self indent;
		write: 'write: {1}' format: { instruction argument }