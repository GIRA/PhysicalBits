encoding instructions
visitJNZInstruction: instruction
	self indent;
		write: 'jnz: {1}' format: { instruction argument }