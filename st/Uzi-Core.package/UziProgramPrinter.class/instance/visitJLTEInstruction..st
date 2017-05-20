encoding instructions
visitJLTEInstruction: instruction
	self indent;
		saveInterval: [self write: 'jlte: {1}' format: { instruction argument }]