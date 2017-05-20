encoding instructions
visitTurnOffInstruction: instruction
	self indent;
		saveInterval: [self write: 'turnOff: {1}' format: { instruction argument }]