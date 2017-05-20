encoding instructions
visitTurnOnInstruction: instruction
	self indent;
		saveInterval: [self write: 'turnOn: {1}' format: { instruction argument }]