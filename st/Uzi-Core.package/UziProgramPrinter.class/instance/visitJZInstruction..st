encoding instructions
visitJZInstruction: instruction
	self indent;
		saveInterval: [self write: 'jz: {1}' format: { instruction argument }]