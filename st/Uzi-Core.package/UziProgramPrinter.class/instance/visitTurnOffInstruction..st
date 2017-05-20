encoding instructions
visitTurnOffInstruction: instruction
	self indent;
		write: 'turnOff: {1}' format: { instruction argument }