encoding instructions
visitTurnOnInstruction: instruction
	self indent;
		write: 'turnOn: {1}' format: { instruction argument }