encoding instructions
visitReadLocalInstruction: instruction
	self indent;
		write: 'push: #{1}' format: { instruction argument name }