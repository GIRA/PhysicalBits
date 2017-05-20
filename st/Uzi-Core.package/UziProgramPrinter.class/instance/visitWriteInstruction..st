encoding instructions
visitWriteInstruction: instruction
	self indent;
		saveInterval: [self write: 'write: {1}' format: { instruction argument }]