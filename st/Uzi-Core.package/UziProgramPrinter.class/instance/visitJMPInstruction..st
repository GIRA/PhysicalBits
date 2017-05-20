encoding instructions
visitJMPInstruction: instruction
	self indent;
		saveInterval: [self write: 'jmp: {1}' format: { instruction argument }]