encoding instructions
visitJNZInstruction: instruction
	self indent;
		saveInterval: [self write: 'jnz: {1}' format: { instruction argument }]