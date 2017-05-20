encoding instructions
visitReadLocalInstruction: instruction
	self indent;
		saveInterval: [self write: 'push: #{1}' format: { instruction argument name }]